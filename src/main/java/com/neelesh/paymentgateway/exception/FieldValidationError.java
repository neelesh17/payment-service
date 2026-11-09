package com.neelesh.paymentgateway.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class FieldValidationError {
    private String field;
    private String reason;
}
