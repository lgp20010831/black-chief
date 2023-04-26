package com.black.nexus;

import java.io.InputStream;

public interface FileStreamReader {


    void handle(InputStream inputStream, String fileName) throws Throwable;


}
