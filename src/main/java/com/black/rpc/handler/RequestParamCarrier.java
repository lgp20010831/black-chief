package com.black.rpc.handler;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

@Getter
public class RequestParamCarrier {

    private Object writeBody;

    private JSONObject requestJson;

    public void initRequestJson(){
        requestJson = new JSONObject();
    }

    public boolean isWriteBody(){
        return writeBody != null;
    }

    public void setWriteBody(Object writeBody) {
        this.writeBody = writeBody;
    }

    public Object getRequestParam(){
        return isWriteBody() ? writeBody : requestJson;
    }
}
