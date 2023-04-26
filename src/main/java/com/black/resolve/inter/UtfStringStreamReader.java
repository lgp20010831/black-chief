package com.black.resolve.inter;

import com.black.io.in.JHexByteArrayInputStream;

import java.io.IOException;

public class UtfStringStreamReader implements StringStreamReader{

    @Override
    public String readString(JHexByteArrayInputStream inputStream) throws IOException {
        return inputStream.readUnrestrictedUtf();
    }
}
