package com.neelesh.paymentgateway.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.neelesh.paymentgateway.enums.ErrorCodes;
import lombok.*;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class ErrorResponse {
    private String traceId;
    private String path;
    private String message;
    private Instant timestamp;
    private int errorCode;
    private ErrorCodes errorCodeEnum;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FieldValidationError> fieldErrors;
}
