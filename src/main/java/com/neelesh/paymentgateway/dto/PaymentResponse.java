package com.neelesh.paymentgateway.dto;

import com.neelesh.paymentgateway.enums.PaymentStatus;
import jakarta.annotation.Nullable;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class PaymentResponse {
    String id;
    String payeeReference;
    String payerUsername;
    Long amount;
    @Builder.Default
    Long refundedAmount = 0L;
    String currency;
    PaymentStatus status;
    String description;
    Map<String, String> metadata;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
