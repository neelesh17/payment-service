package com.neelesh.paymentgateway.service;

import com.neelesh.paymentgateway.model.Payment;

public record RefundResult(Payment payment, String username) {
}
