package com.example.demo_springboot.restservice.exceptionsAndAdvices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BatteryNotFoundAdvice {

  @ExceptionHandler(BatteryNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String batteryNotFoundHandler(BatteryNotFoundException ex) {
    return ex.getMessage();
  }
}
