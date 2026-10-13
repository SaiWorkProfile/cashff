package com.cashff.controller;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
 private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException e) {
  return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()==null?"Invalid request":e.getMessage()));
 }

 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<Map<String,String>> validation(MethodArgumentNotValidException e) {
  String message = e.getBindingResult().getFieldErrors().stream()
   .map(error -> error.getField() + ": " + error.getDefaultMessage())
   .findFirst()
   .orElse("Please check the submitted values.");
  return ResponseEntity.badRequest().body(Map.of("message",message));
 }

 @ExceptionHandler(ConstraintViolationException.class)
 public ResponseEntity<Map<String,String>> constraintViolation(ConstraintViolationException e) {
  String message = e.getConstraintViolations().stream()
   .map(violation -> violation.getMessage())
   .findFirst()
   .orElse("Please check the submitted values.");
  return ResponseEntity.badRequest().body(Map.of("message",message));
 }

 @ExceptionHandler(MaxUploadSizeExceededException.class)
 public ResponseEntity<Map<String,String>> uploadTooLarge(MaxUploadSizeExceededException e) {
  return ResponseEntity.badRequest().body(Map.of("message","Uploaded file is too large."));
 }

 @ExceptionHandler(ResponseStatusException.class)
 public ResponseEntity<Map<String,String>> status(ResponseStatusException e) {
  return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Request failed":e.getReason()));
 }

 @ExceptionHandler(DataAccessException.class)
 public ResponseEntity<Map<String,String>> database(DataAccessException e) {
  logger.error("Database operation failed",e);
  return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
   .body(Map.of("message","The database operation failed. Verify that MySQL is available and the database schema is up to date."));
 }

 @ExceptionHandler(Exception.class)
 public ResponseEntity<Map<String,String>> generic(Exception e) {
  logger.error("Unhandled API request failure",e);
  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
   .body(Map.of("message","The server could not complete the request. Check the backend logs for details."));
 }
}
