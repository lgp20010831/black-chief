package com.black.treaty.rtp;

import com.alibaba.fastjson.JSONObject;
import com.black.io.out.JHexByteArrayOutputStream;
import lombok.Setter;

@Setter
public class BaseRtpResponse implements RtpResponse{

    private JSONObject headerJson;

    private final JHexByteArrayOutputStream dataOutputStream;

    public BaseRtpResponse() {
        dataOutputStream = new JHexByteArrayOutputStream();
    }

    @Override
    public JSONObject getHeaderJson() {
        return headerJson;
    }

    @Override
    public JHexByteArrayOutputStream getDataOutputStream() {
        return dataOutputStream;
    }
}
