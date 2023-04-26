package com.black.io.out;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.throwable.IOSException;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("all") @NoArgsConstructor
public class BinaryFile implements BinaryPartElement{

    private byte[] buf;

    private String name;

    private String fileName;

    public BinaryFile(String name, String fileName, InputStream inputStream){
        this.name = name;
        this.fileName = fileName;
        try {
            buf = new JHexByteArrayInputStream(inputStream).readAll();
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }

    public BinaryFile(String name, File file){
        this.name = name;
        this.fileName = file.getName();
        try {
            buf = new JHexByteArrayInputStream(new FileInputStream(file)).readAll();
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }

    public BinaryFile(String name, String fileName, byte[] buf){
        this.name = name;
        this.buf = buf;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JHexByteArrayInputStream getInputStream() {
        return new JHexByteArrayInputStream(buf);
    }

    @Override
    public int size() throws IOException {
        return buf.length;
    }

    @Override
    public byte[] buf() {
        return buf;
    }
}
