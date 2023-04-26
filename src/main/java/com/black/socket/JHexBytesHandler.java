package com.black.socket;

import com.black.io.in.JHexByteArrayInputStream;

public interface JHexBytesHandler extends JHexThrowableHandler{

    void resolveBytes(JHexByteArrayInputStream in, JHexSocket socket) throws Throwable;



}
