package com.example.demo_springboot.restservice.exceptionsAndAdvices;

import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InvalidPostcodeRangeAdvice {

  @ExceptionHandler(InvalidPostcodeRangeException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ResponseEntity<?> handleInvalidPostcodeRange(InvalidPostcodeRangeException ex) {
    return ResponseEntity
        .badRequest()
        .body(Problem.create()
            .withTitle("Invalid Postcode Range")
            .withDetail(ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleUnreadableRequest(HttpMessageNotReadableException ex) {
      return ResponseEntity
          .badRequest()
          .body(Problem.create()
              .withTitle("Malformed Request")
              .withDetail("Request body is missing or invalid."));
  }
}
