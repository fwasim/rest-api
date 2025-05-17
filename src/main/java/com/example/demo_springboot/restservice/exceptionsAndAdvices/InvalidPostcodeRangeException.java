package com.example.demo_springboot.restservice.exceptionsAndAdvices;

public class InvalidPostcodeRangeException extends RuntimeException {
    public InvalidPostcodeRangeException(String message) {
        super(message);
    }
}