package com.black.out_resolve.impl;

import com.black.io.out.JHexByteArrayOutputStream;

import java.io.OutputStream;

public abstract class AbstractJHexOutputStreamResolver extends AbstractOutputStreamResolver{


    @Override
    public void doResolve(OutputStream outputStream, Object rack, Object value) throws Throwable {
        JHexByteArrayOutputStream jHexOut = new JHexByteArrayOutputStream(outputStream);
        resolveJHex(jHexOut, rack, value);
        jHexOut.flush();
    }

    protected abstract void resolveJHex(JHexByteArrayOutputStream outputStream, Object rack, Object value) throws Throwable;
}
