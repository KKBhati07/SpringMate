package com.example.SpringMate.Shared.Exception;

import com.example.SpringMate.Auth.Exception.TooManyRequestsException;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.User.Exception.UnauthorizedUserUpdateException;
import com.example.SpringMate.User.Exception.UserAlreadyExistsException;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.User.Exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

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

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Response<?>> handleNoHandler(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new Response<>(null, "Do not have access to the resource"));
    }



    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Response<?>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Response<?>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedUserUpdateException.class)
    public ResponseEntity<Response<?>> handleUnauthorizedUserUpdate(UnauthorizedUserUpdateException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Response<?>> handleTooManyRequests(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Response<?>> handleResourceNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Response<?>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Response<>(null, ex.getMessage()));
    }


//    @ExceptionHandler(InvalidListingException.class)
//    public ResponseEntity<Response<?>> handleInvalidListing(InvalidListingException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(new Response<>(null, ex.getMessage()));
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response<>(null, Constants.Messages.Error.SOMETHING_WENT_WRONG));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Response<String>> handleMissingHeader(MissingRequestHeaderException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new Response<>(null, "Missing required header: " + ex.getHeaderName()));
    }

}
