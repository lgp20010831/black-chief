package com.black.nexus;

import com.black.core.minio.Minios;

import java.io.InputStream;

public class MinioFileReader implements FileStreamReader{

    @Override
    public void handle(InputStream inputStream, String fileName) throws Throwable {
        Minios.def().upload(inputStream, fileName, null);
    }
}
