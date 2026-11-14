package com.neelesh.paymentgateway.model;

import com.neelesh.paymentgateway.enums.DeliveryStatus;
import com.neelesh.paymentgateway.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(
        name="outbox_event",
        indexes = {
                @Index(name = "idx_outbox_status_next_attempt", columnList = "delivery_status,next_attempt_at"),
                @Index(name = "idx_outbox_payment_id", columnList = "payment_id"),
                @Index(name = "idx_outbox_user_id", columnList = "user_id"),
                @Index(name = "idx_outbox_status_claimed", columnList = "delivery_status,claimed_at")
        })
public class OutboxEvent {
    @Id
    @Column(length=30, nullable = false)
    String id;
    @Column(nullable = false)
    String paymentId;
    @Column(nullable = false)
    Long userId;
    @Column(columnDefinition = "TEXT", nullable = false)
    String payload;
    @Column(nullable = false)
    Instant nextAttemptAt;
    Instant claimedAt;
    @Column(nullable = false)
    @Builder.Default
    Instant createdAt = Instant.now();
    @Column(nullable = false)
    int attemptCount;
    String lastError;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EventType eventType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DeliveryStatus deliveryStatus;
    @Version
    Long version;
}
