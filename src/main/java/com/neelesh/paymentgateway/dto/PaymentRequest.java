package com.neelesh.paymentgateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class PaymentRequest {
    @NotBlank
    String payeeId;
    @NotBlank
    String paymentMethodId;
    @Positive @NotNull
    Long amount;
    @NotBlank
    String currency;
    String description;
    private Map<String, String> metadata;

}
