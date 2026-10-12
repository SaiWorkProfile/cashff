package com.cashff.controller;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException; import org.springframework.web.bind.MethodArgumentNotValidException; import jakarta.validation.ConstraintViolationException; import org.springframework.web.multipart.MaxUploadSizeExceededException; import java.util.Map;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()==null?"Invalid request":e.getMessage()));}
 @ExceptionHandler({MethodArgumentNotValidException.class,ConstraintViolationException.class}) public ResponseEntity<Map<String,String>> validation(Exception e){return ResponseEntity.badRequest().body(Map.of("message","Please check the submitted values."));}
 @ExceptionHandler(MaxUploadSizeExceededException.class) public ResponseEntity<Map<String,String>> uploadTooLarge(MaxUploadSizeExceededException e){return ResponseEntity.badRequest().body(Map.of("message","Uploaded file is too large."));}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<Map<String,String>> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"Request failed":e.getReason()));}
 @ExceptionHandler(Exception.class) public ResponseEntity<Map<String,String>> generic(Exception e){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Request could not be completed."));}
}
