package com.neelesh.paymentgateway.controller;

import com.neelesh.paymentgateway.dto.PaymentRequest;
import com.neelesh.paymentgateway.dto.PaymentResponse;
import com.neelesh.paymentgateway.dto.RefundRequest;
import com.neelesh.paymentgateway.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> createPayment(
            @Valid @RequestBody PaymentRequest paymentRequest,
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            //placeholder
            @RequestHeader(value = "Payer-Id", required = true) String payerId
    ) {
        try {
            PaymentResponse response = paymentService.createPayment(paymentRequest, payerId, idempotencyKey);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while fetching user: " + e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentStatus(
            @PathVariable String id
    ) {
        try {
            PaymentResponse response = paymentService.getPaymentStatus(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while fetching status: " + e);
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<?> initateRefund(
            @PathVariable String id,
            @RequestHeader(value = "Idempotency-Key", required = true) String idempotencyKey,
            @RequestBody RefundRequest refundRequest
    ){
        try{
            PaymentResponse response = paymentService.intiateRefund(id, idempotencyKey,refundRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }catch(Exception e){
            return ResponseEntity.internalServerError().body("An error occurred while initiating refund: " + e);
        }
    }
}
