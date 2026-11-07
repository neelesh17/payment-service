package com.neelesh.paymentgateway.dto;

import com.neelesh.paymentgateway.enums.PaymentStatus;
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
    String payeeId;
    String payerId;
    Long amount;
    String currency;
    PaymentStatus status;
    String description;
    Map<String, String> metadata;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
