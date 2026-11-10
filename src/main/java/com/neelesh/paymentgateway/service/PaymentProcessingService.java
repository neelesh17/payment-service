package com.neelesh.paymentgateway.service;

import com.neelesh.paymentgateway.enums.PaymentStatus;
import com.neelesh.paymentgateway.exception.InvalidPaymentStatusException;
import com.neelesh.paymentgateway.exception.PaymentNotFoundException;
import com.neelesh.paymentgateway.exception.RefundExceedsRemainingException;
import com.neelesh.paymentgateway.model.Payment;
import com.neelesh.paymentgateway.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
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

    @Transactional
    public Payment processRefund(String id, Long refundAmount){
        Optional<Payment> payment = paymentRepository.findByIdForUpdate(id);
        if (payment.isEmpty()) {
            throw new PaymentNotFoundException(id);
        }
        if(payment.get().getStatus() == PaymentStatus.SUCCESS || payment.get().getStatus() == PaymentStatus.PARTIALLY_REFUNDED){
            if(refundAmount.equals(payment.get().getAmount() - payment.get().getRefundedAmount())){
                payment.get().setStatus(PaymentStatus.REFUNDED);
            }else if (refundAmount.compareTo(payment.get().getAmount() - payment.get().getRefundedAmount()) < 0) {
                payment.get().setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }else{
                throw new RefundExceedsRemainingException(payment.get().getId(), payment.get().getAmount() - payment.get().getRefundedAmount());
            }
            payment.get().setRefundedAmount(payment.get().getRefundedAmount() + refundAmount);
            //not neseccary because inside transaction a loaded entity is managed and changes flush at commit even without save
            paymentRepository.save(payment.get());
            ledgerService.recordRefund(payment.get(), refundAmount);
            return payment.get();
        }else{
            throw new InvalidPaymentStatusException(payment.get().getStatus(),id);
        }
    }
}
