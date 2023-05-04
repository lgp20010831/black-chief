package com.black.share;

import lombok.Data;

/**
 * @author shkstart
 * @create 2023-05-04 10:44
 */
@SuppressWarnings("ALL") @Data
public class InvokeMethodRequest extends AbstractRequest{

    private final String methodName;

    private final Object[] args;

    public InvokeMethodRequest(String methodName, Object[] args) {
        this.methodName = methodName;
        this.args = args;
    }
}
