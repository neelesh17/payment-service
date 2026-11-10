package com.neelesh.paymentgateway.model;

import com.neelesh.paymentgateway.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(name = "payment")
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Payment {
    @Id
    String id;

    String payeeId;
    String payerId;
    String paymentMethodId;
    Long amount;
    @Builder.Default
    @Column(nullable = false, columnDefinition = "bigint default 0")
    Long refundedAmount=0L;
    String currency;
    @Enumerated(EnumType.STRING)
    PaymentStatus status;
    String description;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, String> metadata;

    String providerReference;
    String idempotencyKey;
    @Version
    Long version;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        id = "pay_" + UUID.randomUUID().toString();
        if(status == null) status  = PaymentStatus.INITIATED;
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
