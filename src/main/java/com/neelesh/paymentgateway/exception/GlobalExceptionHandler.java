package com.neelesh.paymentgateway.exception;

import com.neelesh.paymentgateway.enums.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.UUID;
import static java.util.stream.Collectors.toList;

@Slf4j
@RestControllerAdvice
@NoArgsConstructor
public class GlobalExceptionHandler{

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCodeEnum(ErrorCodes.PAYMENT_NOT_FOUND)
                .message(e.getMessage())
                .timestamp(Instant.now())
                .errorCode(HttpStatus.NOT_FOUND.value())
                .path(httpServletRequest.getRequestURI())
                .traceId(traceId)
                .build();
        log.warn("{}: {}, {}", traceId, httpServletRequest.getRequestURI(), e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ServiceTemporarilyUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceTemporarilyUnavailable(ServiceTemporarilyUnavailableException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCodeEnum(e.getErrorCode())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .errorCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .path(httpServletRequest.getRequestURI())
                .traceId(traceId)
                .build();

        log.warn("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentStatus(InvalidPaymentStatusException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCodeEnum(e.getErrorCode())
                .message(e.getMessage())
                .timestamp(Instant.now())
                .errorCode(HttpStatus.CONFLICT.value())
                .path(httpServletRequest.getRequestURI())
                .traceId(traceId)
                .build();

        log.warn("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCodeEnum(ErrorCodes.MISSING_HEADER)
                .message("Request header is missing")
                .timestamp(Instant.now())
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .path(httpServletRequest.getRequestURI())
                .traceId(traceId)
                .build();

        log.warn("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCodeEnum(ErrorCodes.INVALID_JSON)
                .message("Request body is not valid JSON")
                .timestamp(Instant.now())
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .path(httpServletRequest.getRequestURI())
                .traceId(traceId)
                .build();

        log.warn("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCodeEnum(ErrorCodes.INVALID_FIELD)
                .message("One or more fields are invalid/missing")
                .timestamp(Instant.now())
                .errorCode(HttpStatus.BAD_REQUEST.value())
                .path(httpServletRequest.getRequestURI())
                .traceId(traceId)
                .fieldErrors(e.getBindingResult().getFieldErrors().stream().map(fieldError -> FieldValidationError.builder()
                        .field(fieldError.getField())
                        .reason(fieldError.getDefaultMessage())
                        .build())
                        .collect(toList()))
                .build();

        log.warn("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCodeEnum(ErrorCodes.METHOD_NOT_ALLOWED)
                .message("HTTP method not allowed")
                .timestamp(Instant.now())
                .errorCode(HttpStatus.METHOD_NOT_ALLOWED.value())
                .path(httpServletRequest.getRequestURI())
                .traceId(traceId)
                .build();

        log.warn("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCodeEnum(ErrorCodes.RESOURCE_NOT_FOUND)
                .message("Resource not found")
                .timestamp(Instant.now())
                .errorCode(HttpStatus.NOT_FOUND.value())
                .path(httpServletRequest.getRequestURI())
                .traceId(traceId)
                .build();

        log.warn("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCodeEnum(ErrorCodes.INTERNAL_ERROR)
                .message("An unexpected error occurred. Quote the trace ID when reporting it.")
                .timestamp(Instant.now())
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(httpServletRequest.getRequestURI())
                .traceId(traceId)
                .build();

        log.error("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
