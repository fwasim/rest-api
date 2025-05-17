package com.example.demo_springboot;

import com.example.demo_springboot.resourceObjects.Battery;
import com.example.demo_springboot.resourceObjects.BatteryRepository;
import com.example.demo_springboot.resourceObjects.FilterRangeObject;
import com.example.demo_springboot.restservice.BatteryModelAssembler;
import com.example.demo_springboot.restservice.BatteryServiceController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BatteryServiceController.class)
@Import(BatteryServiceControllerTest.TestConfig.class)
class BatteryServiceControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(BatteryServiceControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BatteryRepository batteryRepository;

    @Autowired
    private BatteryModelAssembler batteryModelAssembler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public BatteryRepository batteryRepository() {
            return mock(BatteryRepository.class);
        }

        @Bean
        @Primary
        public BatteryModelAssembler batteryModelAssembler() {
            return new BatteryModelAssembler(); // Use real implementation
        }
    }

    @BeforeEach
    void setup() {
        reset(batteryRepository);
    }

    @Test
    void getBattery_returnsBatteryDetails() throws Exception {
        Battery battery = new Battery("Battery1", "12345", 100L);
        // battery.setId(1L);

        when(batteryRepository.findById(1L)).thenReturn(Optional.of(battery));

        mockMvc.perform(get("/batteryservice/batteries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Battery1"));
    }

    @Test
    void getBattery_notFound_returns404() throws Exception {
        when(batteryRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/batteryservice/batteries/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    void getAllBatteries_returnsListOfBatteries() throws Exception {
        Battery battery = new Battery("Battery1", "12345", 100L);
        // battery.setId(1L);

        when(batteryRepository.findAll()).thenReturn(List.of(battery));

        mockMvc.perform(get("/batteryservice/batteries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.batteryList[0].name").value("Battery1"));
    }

    @Test
    void getAllBatteries_returnsEmptyList() throws Exception {
        when(batteryRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/batteryservice/batteries"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$._embedded.batteries").doesNotExist());;
    }

    @Test
    void newBattery_createsBatteries() throws Exception {
        Battery savedBattery = new Battery("Battery1", "12345", 100L);
        Field idField = Battery.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(savedBattery, 1L);

        when(batteryRepository.saveAll(any())).thenReturn(List.of(savedBattery));

        String json = """
        [
          {"name":"Battery1", "postcode":"12345", "capacity":100}
        ]
        """;

        mockMvc.perform(post("/batteryservice/batteries")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._embedded.batteryList[0].name").value("Battery1"));
    }

    @Test
    void filterByPostcodeRange_withValidRange_returnsStats() throws Exception {
        Battery battery = new Battery("Battery1", "12345", 100L);

        when(batteryRepository.findByPostcodeBetweenOrderByNameAsc("10000", "20000"))
                .thenReturn(List.of(battery));

        FilterRangeObject range = new FilterRangeObject("10000", "20000");

        mockMvc.perform(post("/batteryservice/batteries/bypostcode")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(range)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteries[0].name").value("Battery1"))
                .andExpect(jsonPath("$.totalWattCapacity").value(100))
                .andExpect(jsonPath("$.averageWattCapacity").value(100.0));
    }

    @Test
    void filterByPostcodeRange_withNoResults_returnsEmptyStats() throws Exception {
        when(batteryRepository.findByPostcodeBetweenOrderByNameAsc("10000", "20000"))
                .thenReturn(List.of());

        FilterRangeObject range = new FilterRangeObject("10000", "20000");

        mockMvc.perform(post("/batteryservice/batteries/bypostcode")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(range)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteries").isEmpty())
                .andExpect(jsonPath("$.totalWattCapacity").value(0))
                .andExpect(jsonPath("$.averageWattCapacity").value(0.0));
    }

    @Test
    void filterByPostcodeRange_withInvalidPostcode_returnsBadRequest() throws Exception {
        FilterRangeObject invalidRange = new FilterRangeObject("abc", "xyz");

        mockMvc.perform(post("/batteryservice/batteries/bypostcode")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRange)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void concurrentPostRequests_areHandledCorrectly() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Battery savedBattery = new Battery("Battery1", "12345", 100L);
        Field idField = Battery.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(savedBattery, 1L);

        when(batteryRepository.saveAll(any())).thenReturn(List.of(savedBattery));

        String json = """
        [
          {"name":"Battery1", "postcode":"12345", "capacity":100}
        ]
        """;

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(post("/batteryservice/batteries")
                                    .contentType("application/json")
                                    .content(json))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        verify(batteryRepository, times(threadCount)).saveAll(any());
    }
}
