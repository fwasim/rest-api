package com.example.demo_springboot;

import com.example.demo_springboot.resourceObjects.Battery;
import com.example.demo_springboot.resourceObjects.BatteryListRequest;
import com.example.demo_springboot.resourceObjects.BatteryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class BatteryServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BatteryRepository batteryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        batteryRepository.deleteAll();
    }

    @Test
    void testCreateAndRetrieveBattery() throws Exception {
        Battery battery = new Battery("BatteryX", "11111", 200L);
        var payload = new BatteryListRequest(List.of(battery));

        System.out.println("Test!");
        System.out.println(objectMapper.writeValueAsString(payload));
        mockMvc.perform(post("/batteryservice/batteries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/batteryservice/batteries"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.batteryList[0].name").value("BatteryX"));
    }

    @Test
    void testFilterByPostcodeRangeIntegration() throws Exception {
        Battery b1 = new Battery("BatteryA", "10100", 150L);
        Battery b2 = new Battery("BatteryB", "10500", 250L);
        batteryRepository.saveAll(List.of(b1, b2));

        String json = """
        {
            "from": "10000",
            "to": "11000"
        }
        """;

        mockMvc.perform(post("/batteryservice/batteries/bypostcode")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteries[0].name").value("BatteryA"))
                .andExpect(jsonPath("$.totalWattCapacity").value(400))
                .andExpect(jsonPath("$.averageWattCapacity").value(200.0));
    }
}

