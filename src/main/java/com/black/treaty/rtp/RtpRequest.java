package com.black.treaty.rtp;

import com.alibaba.fastjson.JSONObject;
import com.black.io.in.JHexByteArrayInputStream;

public interface RtpRequest {

    String getAddress();

    JSONObject getHeaderJson();

    default String getHeader(String key){
        return getHeaderJson().getString(key);
    }

    JHexByteArrayInputStream getDataInputStream();


}
