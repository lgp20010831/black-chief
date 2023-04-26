package com.black.treaty.rtp;

import com.alibaba.fastjson.JSONObject;
import com.black.io.in.JHexByteArrayInputStream;
import lombok.Setter;

@Setter
public class BaseRtpRequest implements RtpRequest{

    private String address;

    private JSONObject headerJson;

    private JHexByteArrayInputStream inputStream;

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public JSONObject getHeaderJson() {
        return headerJson;
    }

    @Override
    public JHexByteArrayInputStream getDataInputStream() {
        return inputStream;
    }
}
