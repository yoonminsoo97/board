package com.board.global.common.dto;

import com.board.global.error.ErrorType;

import lombok.Getter;

import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ErrorResponse {

   private final LocalDateTime timeStamp;
   private final String path;
   private final Error error;

   private ErrorResponse(ErrorType errorType, String path) {
       this.timeStamp = LocalDateTime.now();
       this.path = path;
       this.error = new Error(errorType);
   }

   private ErrorResponse(ErrorType errorType, String path, BindingResult bindingResult) {
       this.timeStamp = LocalDateTime.now();
       this.path = path;
       this.error = new Error(errorType, bindingResult);
   }

   public static ErrorResponse of(ErrorType errorType, String path) {
       return new ErrorResponse(errorType, path);
   }

   public static ErrorResponse of(ErrorType errorType, String path, BindingResult bindingResult) {
       return new ErrorResponse(errorType, path, bindingResult);
   }

   @Getter
   private static class Error {

       private final String code;
       private final String message;
       private final List<FieldError> fieldErrors;

       public Error(ErrorType errorType) {
           this.code = errorType.getErrorCode();
           this.message = errorType.getMessage();
           this.fieldErrors = new ArrayList<>();
       }

       public Error(ErrorType errorType, BindingResult bindingResult) {
           this.code = errorType.getErrorCode();
           this.message = errorType.getMessage();
           this.fieldErrors = FieldError.of(bindingResult);
       }

       @Getter
       private static class FieldError {

           private final String field;
           private final String input;
           private final String message;

           private FieldError(org.springframework.validation.FieldError fieldError) {
               this.field = fieldError.getField();
               this.input = fieldError.getRejectedValue() == null ? "" : fieldError.getRejectedValue().toString();
               this.message = fieldError.getDefaultMessage();
           }

           public static List<FieldError> of(BindingResult bindingResult) {
               return bindingResult.getFieldErrors().stream()
                       .map(FieldError::new)
                       .collect(Collectors.toList());
           }

       }
   }

}
