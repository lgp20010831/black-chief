package com.black.core.factory.beans.xml;

import java.io.InputStream;

public interface ResourceReader {

    InputStream reader(String path);
}
