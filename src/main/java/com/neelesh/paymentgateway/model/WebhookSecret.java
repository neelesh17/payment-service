package com.neelesh.paymentgateway.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(
        name = "webhook_secret",
        indexes = {
            @Index(name = "idx_webhook_secret_user_id_active", columnList = "user_id, active"),
        })
@AllArgsConstructor
public class WebhookSecret {
    @Id
    String id;
    @Column(nullable = false)
    Long userId;
    @Column(length = 100, nullable = false)
    String encryptedSecret;
    @Column(nullable = false)
    @Builder.Default
    Instant createdAt = Instant.now();
    @Column(nullable = false)
    boolean active ;
}
