package com.neelesh.paymentgateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundRequest {
    @Positive @NotNull
    Long amount;
    @NotBlank
    String reason;
}
