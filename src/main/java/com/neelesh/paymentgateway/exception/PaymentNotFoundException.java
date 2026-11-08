package com.neelesh.paymentgateway.exception;

public class PaymentNotFoundException extends RuntimeException{
    public PaymentNotFoundException(String paymentId){
        super("Payment Status for id "+ paymentId +" not found. Id does not exist.");
    }
}
