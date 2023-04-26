package com.black.rpc.ill;

public class MethodInvokerParamWiredException extends RuntimeException{


    public MethodInvokerParamWiredException() {
    }

    public MethodInvokerParamWiredException(String message) {
        super(message);
    }

    public MethodInvokerParamWiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodInvokerParamWiredException(Throwable cause) {
        super(cause);
    }
}
