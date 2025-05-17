package com.example.demo_springboot.restservice.exceptionsAndAdvices;

public class BatteryNotFoundException extends RuntimeException {
    public BatteryNotFoundException(Long id) {
        super("Could not find battery with id " + id);
    }
}
