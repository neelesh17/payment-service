package com.neelesh.paymentgateway.service;

import com.neelesh.paymentgateway.dto.PaymentRequest;
import com.neelesh.paymentgateway.dto.PaymentResponse;
import com.neelesh.paymentgateway.dto.RefundRequest;
import com.neelesh.paymentgateway.enums.ErrorCodes;
import com.neelesh.paymentgateway.enums.PaymentStatus;
import com.neelesh.paymentgateway.exception.PaymentNotFoundException;
import com.neelesh.paymentgateway.exception.ServiceTemporarilyUnavailableException;
import com.neelesh.paymentgateway.model.Payment;
import com.neelesh.paymentgateway.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentService {

    private final IdempotencyService idempotencyService;
    private final PaymentProcessingService paymentProcessingService;
    private final PaymentRepository paymentRepository;
    private final LedgerService ledgerService;
    private static final int MAX_RETRIES = 5;
    private static final int SLEEP_BEFORE_RETRY = 10;

    public PaymentResponse createPayment(PaymentRequest req, String payerId, String idempotencyKey){
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

            Payment payment = Payment.builder()
                    .payerId(payerId)
                    .payeeId(req.getPayeeId())
                    .description(req.getDescription())
                    .amount(req.getAmount())
                    .currency(req.getCurrency())
                    .idempotencyKey(idempotencyKey)
                    .paymentMethodId(req.getPaymentMethodId())
                    .metadata(req.getMetadata())
                    .status(PaymentStatus.INITIATED).build();
            paymentRepository.save(payment);
            Payment processedPayment = paymentProcessingService.processPayment(payment);
            PaymentResponse response = PaymentResponse.builder()
                    .payerId(processedPayment.getPayerId())
                    .amount(processedPayment.getAmount())
                    .currency(processedPayment.getCurrency())
                    .metadata(processedPayment.getMetadata())
                    .description(processedPayment.getDescription())
                    .id(processedPayment.getId())
                    .status(processedPayment.getStatus())
                    .payeeId(processedPayment.getPayeeId())
                    .createdAt(processedPayment.getCreatedAt())
                    .updatedAt(processedPayment.getUpdatedAt()).build();
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

        return PaymentResponse.builder()
                .payerId(payment.get().getPayerId())
                .amount(payment.get().getAmount())
                .currency(payment.get().getCurrency())
                .metadata(payment.get().getMetadata())
                .description(payment.get().getDescription())
                .id(payment.get().getId())
                .status(payment.get().getStatus())
                .payeeId(payment.get().getPayeeId())
                .createdAt(payment.get().getCreatedAt())
                .updatedAt(payment.get().getUpdatedAt()).build();
    }

    public PaymentResponse intiateRefund(String id, String idempotencyKey, RefundRequest refundRequest){
        try{
            Optional<PaymentResponse> res = idempotencyService.getExistingResponse(idempotencyKey);
            if(res.isPresent()) return res.get();
            boolean isLockAcquired =false;
            int reties=0;
            for(;reties<MAX_RETRIES && !isLockAcquired;reties++) {

                res = idempotencyService.getExistingResponse(idempotencyKey);
                if(res.isPresent()) return res.get();
                isLockAcquired = idempotencyService.tryAcquireLock(idempotencyKey);
                if(!isLockAcquired) TimeUnit.SECONDS.sleep(SLEEP_BEFORE_RETRY);
            }
            if(reties==MAX_RETRIES){
                throw new RuntimeException("Error while aquirng lock");
            }
            Payment payment = paymentRepository.getReferenceById(id);
            if(payment.getStatus() == PaymentStatus.SUCCESS){
                if(refundRequest.getAmount().equals(payment.getAmount())){
                    payment.setStatus(PaymentStatus.REFUNDED);
                }else{
                    payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
                }
                paymentRepository.save(payment);
                ledgerService.recordRefund(payment, refundRequest.getAmount());
                PaymentResponse response = PaymentResponse.builder()
                        .payerId(payment.getPayerId())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .metadata(payment.getMetadata())
                        .description(refundRequest.getReason())
                        .id(payment.getId())
                        .status(payment.getStatus())
                        .payeeId(payment.getPayeeId())
                        .createdAt(payment.getCreatedAt())
                        .updatedAt(payment.getUpdatedAt()).build();
                idempotencyService.storeResponse(idempotencyKey, response,payment.getStatus());
                return response;
            }else{
                throw new RuntimeException("Payment not succes, refund cannot be initaited");
            }
        } finally {
            idempotencyService.releaseLock(idempotencyKey);
        }
    }
}
