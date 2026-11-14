package com.neelesh.paymentgateway.exception;

public class PayerNotFoundException extends RuntimeException {
    public PayerNotFoundException(String username) {
        super("Payer not found with username: " + username);
    }
}
