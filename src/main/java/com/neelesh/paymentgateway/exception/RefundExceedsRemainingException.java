package com.neelesh.paymentgateway.exception;

public class RefundExceedsRemainingException extends RuntimeException{
    public RefundExceedsRemainingException(String id, Long remainingAmount) {
        super("Refund amount exceeds the remaining amount, for payment id " + id + " with remaining max refundable amount " + remainingAmount);
    }
}
