package com.neelesh.paymentgateway.model;

import com.neelesh.paymentgateway.enums.PaymentStatus;
import io.lettuce.core.dynamic.annotation.CommandNaming;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(name = "payment")
@AllArgsConstructor
@Getter
@Setter
public class Payment {
    @Id
    String id;

    String payeeId;
    String payerId;
    String paymentMethod;
    Long amount;
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
