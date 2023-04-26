package com.black.rpc.request;

import com.alibaba.fastjson.JSONObject;
import com.black.io.out.DataByteBufferArrayOutputStream;

import java.io.IOException;

public class JsonRequest extends AbstractBaseRequest{

    public static final String REQUEST_METHOD_NAME = "request-method";
    public static final String REQUEST_ID_NAME = "request-id";
    public static final String REQUEST_PARAM = "request-param";
    public static final String REQUEST_BODY_SIZE = "request-body-size";


    private JSONObject requestJson = new JSONObject();

    public JSONObject getRequestJson() {
        return requestJson;
    }

    @Override
    public String getRequestId() {
        return requestJson.getString(REQUEST_ID_NAME);
    }

    @Override
    public String getMethodName() {
        return requestJson.getString(REQUEST_METHOD_NAME);
    }

    @Override
    public byte[] toByteArray() throws IOException {
        DataByteBufferArrayOutputStream out = new DataByteBufferArrayOutputStream();
        out.writeInt(getRequestType().getType());
        out.writeUTF(requestJson.toString());
        return out.toByteArray();
    }

    @Override
    public Object getParam() {
        return requestJson.get(REQUEST_PARAM);
    }

    @Override
    public int getBodySize() {
        return requestJson.getIntValue(REQUEST_BODY_SIZE);
    }

    @Override
    public void addParam(String newBody) {
        Object param = getParam();
        requestJson.put(REQUEST_PARAM, param + newBody);
    }
}
