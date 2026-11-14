package com.neelesh.paymentgateway.service;

import com.neelesh.paymentgateway.dto.PaymentRequest;
import com.neelesh.paymentgateway.dto.PaymentResponse;
import com.neelesh.paymentgateway.dto.RefundRequest;
import com.neelesh.paymentgateway.enums.ErrorCodes;
import com.neelesh.paymentgateway.enums.PaymentStatus;
import com.neelesh.paymentgateway.exception.*;
import com.neelesh.paymentgateway.model.Payment;
import com.neelesh.paymentgateway.model.User;
import com.neelesh.paymentgateway.repository.PaymentRepository;
import com.neelesh.paymentgateway.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentService {

    private final IdempotencyService idempotencyService;
    private final PaymentProcessingService paymentProcessingService;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private static final int MAX_RETRIES = 5;
    private static final int SLEEP_BEFORE_RETRY = 10;


    private PaymentResponse toResponse(Payment payment, String payerUserName){
        return PaymentResponse.builder()
                .payerUsername(payerUserName)
                .amount(payment.getAmount())
                .refundedAmount(payment.getRefundedAmount())
                .currency(payment.getCurrency())
                .metadata(payment.getMetadata())
                .description(payment.getDescription())
                .id(payment.getId())
                .status(payment.getStatus())
                .payeeReference(payment.getPayeeReference())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public PaymentResponse createPayment(PaymentRequest req, String payerUserName, String idempotencyKey){
        Optional<PaymentResponse> res = idempotencyService.getExistingResponse(idempotencyKey);
        if(res.isPresent()) return res.get();
        boolean isLockAcquired =false;
        try {
            int reties=0;
            for(;reties<MAX_RETRIES && !isLockAcquired;reties++) {

                res = idempotencyService.getExistingResponse(idempotencyKey);
                if(res.isPresent()) return res.get();
                isLockAcquired = idempotencyService.tryAcquireLock(idempotencyKey);
                if(!isLockAcquired) TimeUnit.SECONDS.sleep(SLEEP_BEFORE_RETRY);
            }
            if(!isLockAcquired){
                throw new ServiceTemporarilyUnavailableException("Max retires exceeded while acquiring idempotency lock", ErrorCodes.LOCK_ACQUISITION_TIMEOUT);
            }
            res = idempotencyService.getExistingResponse(idempotencyKey);
            if (res.isPresent()) return res.get();
            Optional<User> payer = userRepository.findByUsername(payerUserName);
            if (payer.isEmpty()) {
                throw new PayerNotFoundException(payerUserName);
            }

            Payment payment = Payment.builder()
                    .payerUserId(payer.get().getId())
                    .payeeReference(req.getPayeeReference())
                    .description(req.getDescription())
                    .amount(req.getAmount())
                    .refundedAmount(0L)
                    .currency(req.getCurrency())
                    .idempotencyKey(idempotencyKey)
                    .paymentMethodId(req.getPaymentMethodId())
                    .metadata(req.getMetadata())
                    .status(PaymentStatus.INITIATED).build();
            paymentRepository.save(payment);
            Payment processedPayment = paymentProcessingService.processPayment(payment);
            PaymentResponse response = toResponse(processedPayment, payer.get().getUsername());
            idempotencyService.storeResponse(idempotencyKey, response,processedPayment.getStatus());
            return response;
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceTemporarilyUnavailableException("Lock retry interrupted", ErrorCodes.LOCK_INTERRUPTED, e);
        } finally {
            if (isLockAcquired) {
                idempotencyService.releaseLock(idempotencyKey);
            }
        }
    }

    public PaymentResponse getPaymentStatus(String id){
        Optional<Payment> payment = paymentRepository.findById(id);
        if (payment.isEmpty()) {
            throw new PaymentNotFoundException(id);
        }
        Optional<User> payer = userRepository.findById(payment.get().getPayerUserId());
        if(payer.isEmpty()) {
            throw new PayerNotFoundException("User not Accessible");
        }
        return toResponse(payment.get(), payer.get().getUsername());
    }

    public PaymentResponse intiateRefund(String id, String idempotencyKey, RefundRequest refundRequest){
        boolean isLockAcquired =false;
        try{
            Optional<PaymentResponse> res = idempotencyService.getExistingResponse(idempotencyKey);
            if(res.isPresent()) return res.get();
            int reties=0;
            for(;reties<MAX_RETRIES && !isLockAcquired;reties++) {

                res = idempotencyService.getExistingResponse(idempotencyKey);
                if(res.isPresent()) return res.get();
                isLockAcquired = idempotencyService.tryAcquireLock(idempotencyKey);
                if(!isLockAcquired) TimeUnit.SECONDS.sleep(SLEEP_BEFORE_RETRY);
            }
            if(!isLockAcquired){
                throw new ServiceTemporarilyUnavailableException("Max retires exceeded while acquiring idempotency lock", ErrorCodes.LOCK_ACQUISITION_TIMEOUT);
            }
            res = idempotencyService.getExistingResponse(idempotencyKey);
            if (res.isPresent()) return res.get();

            RefundResult processedPayment = paymentProcessingService.processRefund(id, refundRequest.getAmount());

            PaymentResponse response = toResponse(processedPayment.payment(), processedPayment.username());
            idempotencyService.storeResponse(idempotencyKey, response,processedPayment.payment().getStatus());
            return response;

        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceTemporarilyUnavailableException("Lock retry interrupted", ErrorCodes.LOCK_INTERRUPTED, e);
        } finally {
            if (isLockAcquired) {
                idempotencyService.releaseLock(idempotencyKey);
            }
        }
    }
}
