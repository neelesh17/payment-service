package com.neelesh.paymentgateway.exception;

import com.neelesh.paymentgateway.enums.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
@NoArgsConstructor
public class GlobalExceptionHandler{
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException e, HttpServletRequest httpServletRequest) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCodeEnum(ErrorCodes.PAYMENT_NOT_FOUND)
                .message(e.getMessage())
                .timestamp(Instant.now())
                .errorCode(HttpStatus.NOT_FOUND.value())
                .path(httpServletRequest.getRequestURI())
                .traceId(UUID.randomUUID().toString())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
