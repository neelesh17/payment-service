package com.neelesh.paymentgateway.service;

import com.neelesh.paymentgateway.enums.LedgerType;
import com.neelesh.paymentgateway.model.LedgerEntry;
import com.neelesh.paymentgateway.model.Payment;
import com.neelesh.paymentgateway.repository.LedgerRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LedgerService {
    private final LedgerRepository ledgerRepository;

    @Transactional
    public void recordPayment(Payment payment){
        LedgerEntry credit = LedgerEntry.builder()
                .payment(payment)
                .currency(payment.getCurrency())
                .accountId(payment.getPayeeId())
                .description(payment.getDescription())
                .amount(payment.getAmount())
                .type(LedgerType.CREDIT).build();
        ledgerRepository.save(credit);
        LedgerEntry debit = LedgerEntry.builder()
                .payment(payment)
                .currency(payment.getCurrency())
                .accountId(payment.getPayerId())
                .description(payment.getDescription())
                .amount(payment.getAmount())
                .type(LedgerType.DEBIT).build();
        ledgerRepository.save(debit);
    }

    @Transactional
    public void recordRefund(Payment payment, Long refundAmount){
        LedgerEntry credit = LedgerEntry.builder()
                .payment(payment)
                .currency(payment.getCurrency())
                .accountId(payment.getPayerId())
                .description(payment.getDescription())
                .amount(refundAmount)
                .type(LedgerType.CREDIT).build();
        ledgerRepository.save(credit);
        LedgerEntry debit = LedgerEntry.builder()
                .payment(payment)
                .currency(payment.getCurrency())
                .accountId(payment.getPayeeId())
                .description(payment.getDescription())
                .amount(refundAmount)
                .type(LedgerType.DEBIT).build();
        ledgerRepository.save(debit);
    }
}
