package com.neelesh.paymentgateway.service;

import com.neelesh.paymentgateway.enums.PaymentStatus;
import com.neelesh.paymentgateway.exception.InvalidPaymentStatusException;
import com.neelesh.paymentgateway.exception.PayerNotFoundException;
import com.neelesh.paymentgateway.exception.PaymentNotFoundException;
import com.neelesh.paymentgateway.exception.RefundExceedsRemainingException;
import com.neelesh.paymentgateway.model.Payment;
import com.neelesh.paymentgateway.model.User;
import com.neelesh.paymentgateway.repository.PaymentRepository;
import com.neelesh.paymentgateway.repository.UserRepository;
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
    private final UserRepository userRepository;

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
    public RefundResult processRefund(String id, Long refundAmount){
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
            ledgerService.recordRefund(payment.get(), refundAmount);
            Optional<User> payer = userRepository.findById(payment.get().getPayerUserId());
            if(payer.isEmpty()) {
                throw new PayerNotFoundException(payment.get().getPayerUserId().toString());
            }
            return new RefundResult(payment.get(), payer.get().getUsername());
        }else{
            throw new InvalidPaymentStatusException(payment.get().getStatus(),id);
        }
    }
}
