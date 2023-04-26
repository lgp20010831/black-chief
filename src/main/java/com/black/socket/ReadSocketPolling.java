package com.black.socket;

import com.black.io.in.JHexByteArrayInputStream;

import java.io.IOException;

public interface ReadSocketPolling {


    JHexByteArrayInputStream doRead(JHexByteArrayInputStream socketIn, JHexSocket socket) throws IOException;

}
