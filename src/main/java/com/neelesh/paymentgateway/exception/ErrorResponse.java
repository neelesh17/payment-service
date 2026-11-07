package com.neelesh.paymentgateway.exception;

import com.neelesh.paymentgateway.enums.ErrorCodes;
import lombok.*;
import java.time.Instant;

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
}
