package com.black.treaty;

import com.black.io.out.JHexByteArrayOutputStream;

public interface TreatyCustomHandler {

    void handle(TreatyClient client, byte[] bytes, JHexByteArrayOutputStream out) throws Throwable;


}
