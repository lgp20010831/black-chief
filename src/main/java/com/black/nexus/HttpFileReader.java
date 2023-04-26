package com.black.nexus;

import com.black.core.builder.HttpBuilder;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;

@Log4j2
public class HttpFileReader implements FileStreamReader{
    private final String url;

    public HttpFileReader(String url) {
        this.url = url;
    }


    @Override
    public void handle(InputStream inputStream, String fileName) throws Throwable {
        String body = HttpBuilder.part(url).addInputPart(fileName, inputStream).executeAndGetBody();
        log.info("发送文件流响应: {}", body);
    }
}
