package com.neelesh.paymentgateway.service;

import com.neelesh.paymentgateway.enums.PaymentStatus;
import com.neelesh.paymentgateway.model.Payment;
import com.neelesh.paymentgateway.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@AllArgsConstructor
public class PaymentProcessingService {
    private final LedgerService ledgerService;
    private final PaymentRepository paymentRepository;

    private PaymentStatus callBankAPI(Payment payment){
        Random random = new Random();
        int randomValue = random.nextInt(2);
        return randomValue == 0 ? PaymentStatus.FAILED : PaymentStatus.SUCCESS;
    }

    @Transactional
    Payment processPayment(Payment payment){
        PaymentStatus status = callBankAPI(payment);
        payment.setStatus(status);
        paymentRepository.save(payment);
        if(status == PaymentStatus.SUCCESS) ledgerService.recordPayment(payment);
        return payment;
    }
}
