package com.black.treaty.rtp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.JHexByteArrayOutputStream;

import java.io.IOException;

public class Pattern {

    /*
        请求:
        utf(-1)  json header utf   16进制数据流
        地址      请求头               数据

        响应:
        json header utf  \r\n  16进制数据流
        响应头               换行   数据
     */

    public static RtpRequest createRequest(byte[] bytes) throws IOException {
        BaseRtpRequest baseRtpRequest = new BaseRtpRequest();
        JHexByteArrayInputStream inputStream = new JHexByteArrayInputStream(bytes);
        String address = inputStream.readUTF();
        String headerJson = inputStream.readUTF();
        baseRtpRequest.setAddress(address);
        baseRtpRequest.setHeaderJson(JSON.parseObject(headerJson));
        baseRtpRequest.setInputStream(inputStream);
        return baseRtpRequest;
    }

    public static RtpResponse createBaseResponse(){
        return createBaseResponse(new JSONObject());
    }

    public static RtpResponse createBaseResponse(JSONObject headers){
        BaseRtpResponse baseRtpResponse = new BaseRtpResponse();
        baseRtpResponse.setHeaderJson(headers);
        return baseRtpResponse;
    }

    public static byte[] toByteArray(RtpResponse response) throws IOException {
        JHexByteArrayOutputStream outputStream = new JHexByteArrayOutputStream();
        JSONObject headerJson = response.getHeaderJson();
        outputStream.writeUTF(headerJson.toJSONString());
        outputStream.write(response.getDataOutputStream().toByteArray());
        return outputStream.toByteArray();
    }

}
