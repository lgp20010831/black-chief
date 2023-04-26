package com.black.core.io;

import java.io.IOException;

public interface ObjectReader {

    Object readObject(byte[] buffer) throws IOException;

    Object readObject(byte[] buffer, int offset, int length) throws IOException;
}
