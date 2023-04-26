package com.black.treaty.rtp;

import com.alibaba.fastjson.JSONObject;
import com.black.io.out.JHexByteArrayOutputStream;

public interface RtpResponse {

    JSONObject getHeaderJson();

    default String getHeader(String key){
        return getHeaderJson().getString(key);
    }

    default void writeHeader(String key, String value){
        getHeaderJson().put(key, value);
    }

    JHexByteArrayOutputStream getDataOutputStream();

}
