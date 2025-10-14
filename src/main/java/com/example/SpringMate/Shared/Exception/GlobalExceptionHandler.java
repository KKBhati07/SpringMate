package com.example.SpringMate.Shared.Exception;

import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Util.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<?>> handleValidationErrors(MethodArgumentNotValidException e){
        List<String> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Response<>(Map.of("message", "Validation failed", "errors", errors),"Error"));

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response<>(null, Constants.Messages.Error.SOMETHING_WENT_WRONG));
    }
}
