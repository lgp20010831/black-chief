package com.black.core.io;

import java.io.IOException;

public interface ObjectWriter {

    byte[] writeObject(Object object) throws IOException;
}
