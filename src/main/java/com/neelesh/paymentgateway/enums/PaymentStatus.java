package com.neelesh.paymentgateway.enums;

public enum PaymentStatus {
    INITIATED,PROCESSING,SUCCESS,FAILED,REFUNDED,PARTIALLY_REFUNDED,TIME_OUT;

    public boolean isTerminal(){
        return this == TIME_OUT || this == REFUNDED || this == FAILED;
    }

    public boolean canTransitionTo(PaymentStatus newStatus) {
        return switch(this) {
            case INITIATED -> newStatus == PROCESSING;
            case PROCESSING -> newStatus == SUCCESS || newStatus == FAILED || newStatus == TIME_OUT;
            case SUCCESS -> newStatus == REFUNDED || newStatus == PARTIALLY_REFUNDED;
            case PARTIALLY_REFUNDED -> newStatus == REFUNDED;
            default -> false;
        };
    }
}
