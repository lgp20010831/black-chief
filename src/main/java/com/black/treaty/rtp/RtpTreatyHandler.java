package com.black.treaty.rtp;

import com.black.io.out.JHexByteArrayOutputStream;
import com.black.treaty.TreatyClient;
import com.black.treaty.TreatyCustomHandler;

public class RtpTreatyHandler implements TreatyCustomHandler {

    private final RtpApplication application;

    public RtpTreatyHandler(RtpApplication application) {
        this.application = application;
    }

    @Override
    public void handle(TreatyClient client, byte[] bytes, JHexByteArrayOutputStream out) throws Throwable {
        RtpConfig config = application.getConfig();
        RtpRequest request = Pattern.createRequest(bytes);
        RtpResponse response = Pattern.createBaseResponse();
        //handler request

        client.writeAndFlush(Pattern.toByteArray(response));
    }
}
