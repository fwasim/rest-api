package com.example.demo_springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class DemoSpringbootApplication {
	private static final Logger logger = LoggerFactory.getLogger(DemoSpringbootApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(DemoSpringbootApplication.class, args);
		logger.info("Application launched successfully!");
	}

	@EventListener
    public void handleWebServerReady(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        logger.info("Application is listening on port: {}", port);
    }
}