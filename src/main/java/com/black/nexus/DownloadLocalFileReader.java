package com.black.nexus;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Log4j2
public class DownloadLocalFileReader implements FileStreamReader{

    private final File localDir;

    public DownloadLocalFileReader(String path){
        this(new File(path));
    }

    public DownloadLocalFileReader(File localDir) {
        this.localDir = localDir;
    }

    @Override
    public void handle(InputStream inputStream, String fileName) throws Throwable {
        log.info("下载文件到本地路径:{}, 文件名称:{}", localDir, fileName);
        byte[] byteArr = new byte[1024];
        int len;
        if (!localDir.exists())
            localDir.mkdirs();
        OutputStream outputStream = new FileOutputStream(localDir + File.separator + fileName);
        while ((len = inputStream.read(byteArr)) != -1) {
            outputStream.write(byteArr, 0, len);
        }
        outputStream.close();
        System.out.println("下载文件完成：" + fileName);
    }
}
