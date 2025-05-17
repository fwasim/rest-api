package com.example.demo_springboot.resourceObjects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LoadDatabase {

  private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(BatteryRepository batteryRepository) {
        if (batteryRepository.count() == 0) {
            return args -> {
                ObjectMapper mapper = new ObjectMapper();
                TypeReference<List<Battery>> typeRef = new TypeReference<List<Battery>>() {};

                InputStream inputStream = getClass().getResourceAsStream("/batteries.json");
                if (inputStream == null) {
                    log.error("Could not find batteries.json in classpath!");
                    return;
                }

                List<Battery> batteries = mapper.readValue(inputStream, typeRef);
                batteryRepository.saveAll(batteries);

                batteries.forEach(b -> log.info("Preloaded: " + b));
            };
        }
        else {
            log.info("Battery table already has data. Skipping preload.");
            return args -> {};
        }
    }
}