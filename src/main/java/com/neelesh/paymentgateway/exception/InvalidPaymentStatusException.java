package com.neelesh.paymentgateway.exception;

import com.neelesh.paymentgateway.enums.ErrorCodes;
import com.neelesh.paymentgateway.enums.PaymentStatus;
import lombok.Getter;

@Getter
public class InvalidPaymentStatusException extends RuntimeException {
    private final ErrorCodes errorCode;
    private final PaymentStatus status;

    public InvalidPaymentStatusException(PaymentStatus status, String paymentId) {
        super(buildMessage(status, paymentId));
        this.status = status;
        this.errorCode = resolveErrorCode(status);
    }

    private static ErrorCodes resolveErrorCode(PaymentStatus status) {
        return switch (status) {
            case FAILED, TIME_OUT -> ErrorCodes.NON_REFUNDABLE;
            case REFUNDED -> ErrorCodes.ALREADY_REFUNDED;
            default -> ErrorCodes.TRANSACTION_INITIATED_OR_PROCESSING;
        };
    }

    private static String buildMessage(PaymentStatus status, String paymentId) {
        return switch (status) {
            case FAILED, TIME_OUT -> "Payment " + paymentId + " is " + status + "; refund cannot be initiated.";
            case REFUNDED -> "Payment " + paymentId + " is already refunded.";
            // TODO: PARTIALLY_REFUNDED
            default -> "Payment " + paymentId + " is " + status + "; try again later.";
        };
    }
}