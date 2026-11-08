package com.neelesh.paymentgateway.exception;

import com.neelesh.paymentgateway.enums.ErrorCodes;
import lombok.Getter;

@Getter
public class ServiceTemporarilyUnavailableException extends RuntimeException{
    private final ErrorCodes errorCode;

    public ServiceTemporarilyUnavailableException(String msg, ErrorCodes err){
        super(msg);
        this.errorCode=err;
    }

    public ServiceTemporarilyUnavailableException(String msg, ErrorCodes err, Throwable cause){
        super(msg, cause);
        this.errorCode=err;
    }
}
