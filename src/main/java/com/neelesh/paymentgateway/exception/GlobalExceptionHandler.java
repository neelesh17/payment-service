package com.neelesh.paymentgateway.exception;

import com.neelesh.paymentgateway.enums.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.CannotCreateTransactionException;
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

    private ErrorResponse buildErrorResponse(ErrorCodes errorCode, String msg, HttpStatus httpStatus, HttpServletRequest req, String traceId){
        return ErrorResponse.builder()
                .errorCodeEnum(errorCode)
                .message(msg)
                .timestamp(Instant.now())
                .errorCode(httpStatus.value())
                .path(req.getRequestURI())
                .traceId(traceId)
                .build();
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();
        ErrorResponse errorResponse = buildErrorResponse(ErrorCodes.PAYMENT_NOT_FOUND, e.getMessage(), HttpStatus.NOT_FOUND, httpServletRequest, traceId);
        log.warn("{}: {}, {}", traceId, httpServletRequest.getRequestURI(), e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RefundExceedsRemainingException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefundRequest(RefundExceedsRemainingException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();
        ErrorResponse errorResponse = buildErrorResponse(ErrorCodes.REFUND_EXCEEDS_REMAINING, e.getMessage(), HttpStatus.CONFLICT, httpServletRequest, traceId);
        log.warn("{}: {}, {}", traceId, httpServletRequest.getRequestURI(), e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ServiceTemporarilyUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceTemporarilyUnavailable(ServiceTemporarilyUnavailableException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = buildErrorResponse(e.getErrorCode(), e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, httpServletRequest, traceId);

        log.warn("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentStatus(InvalidPaymentStatusException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = buildErrorResponse(e.getErrorCode(), e.getMessage(), HttpStatus.CONFLICT, httpServletRequest, traceId);

        log.warn("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeader(MissingRequestHeaderException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = buildErrorResponse(ErrorCodes.MISSING_HEADER, "Request header is missing", HttpStatus.BAD_REQUEST, httpServletRequest, traceId);

        log.warn("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = buildErrorResponse(ErrorCodes.INVALID_JSON, "Request body is not valid JSON", HttpStatus.BAD_REQUEST, httpServletRequest, traceId);

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

        ErrorResponse errorResponse = buildErrorResponse(ErrorCodes.METHOD_NOT_ALLOWED, "HTTP method not allowed", HttpStatus.METHOD_NOT_ALLOWED, httpServletRequest, traceId);

        log.warn("{}: {}, {}", traceId, httpServletRequest.getRequestURI(), e.getMessage());

        HttpMethod[] supportedMethods = e.getSupportedHttpMethods() != null
                ? e.getSupportedHttpMethods().toArray(new HttpMethod[0])
                : new HttpMethod[0];


        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).allow(supportedMethods).body(errorResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = buildErrorResponse(ErrorCodes.RESOURCE_NOT_FOUND, "Resource not found", HttpStatus.NOT_FOUND, httpServletRequest, traceId);

        log.warn("{}: {}, {}", traceId, httpServletRequest.getRequestURI(), e.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrencyFailureException(ConcurrencyFailureException e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = buildErrorResponse(ErrorCodes.CONCURRENT_MODIFICATION, "Concurrent modification detected", HttpStatus.CONFLICT, httpServletRequest, traceId);

        log.warn("{}: {}, {}", traceId, httpServletRequest.getRequestURI(), e.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({TransientDataAccessException.class, DataAccessResourceFailureException.class, CannotCreateTransactionException.class})
    public ResponseEntity<ErrorResponse> handleTransientDataAccessException(Exception e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = buildErrorResponse(ErrorCodes.RESOURCE_UNAVAILABLE, "Resource unavailable. Please retry after sometime. Quote the trace ID when reporting it.", HttpStatus.SERVICE_UNAVAILABLE, httpServletRequest, traceId);
        log.error("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("Retry-After", "30").body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e, HttpServletRequest httpServletRequest) {
        String traceId = UUID.randomUUID().toString();

        ErrorResponse errorResponse = buildErrorResponse(ErrorCodes.INTERNAL_ERROR, "An unexpected error occurred. Quote the trace ID when reporting it.", HttpStatus.INTERNAL_SERVER_ERROR, httpServletRequest, traceId);
        log.error("{}: {}", traceId, httpServletRequest.getRequestURI(), e);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
