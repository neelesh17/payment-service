package com.neelesh.paymentgateway.model;

import com.neelesh.paymentgateway.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@Table(name="idempotency_key")
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {
    @Id
    String key;
    @Enumerated(EnumType.STRING)
    PaymentStatus status;
    @Column(columnDefinition = "json")
    String response;
    LocalDateTime createdAt;
    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
