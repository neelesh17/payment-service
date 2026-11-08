package com.neelesh.paymentgateway.exception;

import com.neelesh.paymentgateway.enums.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;

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
}
