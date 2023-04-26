package com.black.core.mvc.response;

import com.black.core.aop.servlet.RestResponse;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SimpleResponse implements RestResponse {

    String msg;

    Object result;

    @Override
    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public void setMessage(String message) {
        msg = message;
    }

    @Override
    public Object obtainResult() {
        return result;
    }
}
