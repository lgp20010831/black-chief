package com.black.test;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import lombok.Data;

@Data
public class RecordObject {

    final ClassWrapper<?> cw;

    final MethodWrapper mw;

    final String requestUrl;

    final String requestMethod;

    String requestDemo;

    String responseBody;

    String errorMsg;

    boolean successful = true;

    public RecordObject(ClassWrapper<?> cw, MethodWrapper mw, String requestUrl, String requestMethod) {
        this.cw = cw;
        this.mw = mw;
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        successful = false;
    }
}
