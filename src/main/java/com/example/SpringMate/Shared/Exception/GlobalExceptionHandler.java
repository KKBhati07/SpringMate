package com.example.SpringMate.Shared.Exception;

import com.example.SpringMate.Auth.Exception.TooManyRequestsException;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.User.Exception.UnauthorizedUserUpdateException;
import com.example.SpringMate.User.Exception.UserAlreadyExistsException;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.User.Exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<?>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        log.warn("VALIDATION_ERROR errors={}", ex.getBindingResult().getErrorCount());
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new Response<>(Map.of("message", "Validation failed", "errors", errors), "Error"));

    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Response<?>> handleNoHandler(NoHandlerFoundException ex) {
        log.info("NO_HANDLER");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new Response<>(null, "Do not have access to the resource"));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Response<?>> handleAccessDenied(
            AuthorizationDeniedException ex
    ) {
        log.warn("ACCESS_DENIED");

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new Response<>(null, "Do not have access to the resource"));
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Response<?>> handleUserNotFound(
            UserNotFoundException ex
    ) {
        log.info("USER_NOT_FOUND message={}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Response<?>> handleUserAlreadyExists(
            UserAlreadyExistsException ex
    ) {
        log.warn("USER_ALREADY_EXISTS message={}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedUserUpdateException.class)
    public ResponseEntity<Response<?>> handleUnauthorizedUserUpdate(
            UnauthorizedUserUpdateException ex
    ) {
        log.warn("UNAUTHORIZED_USER_UPDATE");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Response<?>> handleTooManyRequests(
            TooManyRequestsException ex
    ) {
        log.warn("RATE_LIMIT_EXCEEDED message={}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Response<?>> handleResourceNotFound(
            NotFoundException ex
    ) {
        log.info("RESOURCE_NOT_FOUND message={}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Response<?>> handleBadRequest(
            BadRequestException ex
    ) {
        log.warn("BAD_REQUEST message={}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Response<?>> handleUnauthorizedRequest(
            UnauthorizedException ex
    ) {
        log.warn("UNAUTHORIZED");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new Response<>(null, ex.getMessage()));
    }


//    @ExceptionHandler(InvalidListingException.class)
//    public ResponseEntity<Response<?>> handleInvalidListing(InvalidListingException ex) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(new Response<>(null, ex.getMessage()));
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleGeneralException(Exception ex) {
        log.error("UNHANDLED_EXCEPTION", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response<>(null, Constants.Messages.Error.SOMETHING_WENT_WRONG));
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<Response<?>> handleInternalServerException(
            InternalServerException ex
    ) {
        log.error("INTERNAL_SERVER_ERROR ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response<>(null, ex.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Response<String>> handleMissingHeader(
            MissingRequestHeaderException ex
    ) {
        log.warn("MISSING_HEADER header={}", ex.getHeaderName());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new Response<>(null, "Missing required header: " + ex.getHeaderName()));
    }

}
