package com.neelesh.paymentgateway.model;

import com.neelesh.paymentgateway.enums.LedgerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entry")
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    Payment payment;
    String accountId;
    @Enumerated(EnumType.STRING)
    LedgerType type;
    Long amount;
    String currency;
    String description;
    LocalDateTime createdAt;


    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
