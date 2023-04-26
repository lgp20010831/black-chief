package com.black.resolve.inter;

import com.black.io.in.JHexByteArrayInputStream;

import java.io.IOException;

public interface StringStreamReader {

    String readString(JHexByteArrayInputStream inputStream) throws IOException;

}
