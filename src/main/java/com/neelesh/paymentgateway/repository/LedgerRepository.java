package com.neelesh.paymentgateway.repository;

import com.neelesh.paymentgateway.model.LedgerEntry;
import com.neelesh.paymentgateway.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByPayment(Payment payment);
    List<LedgerEntry> findByAccountId(String accountId);
}
