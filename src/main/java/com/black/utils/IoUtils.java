package com.black.utils;

import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.socket.ByteAllocator;
import com.black.core.util.IoUtil;
import com.black.core.util.StringUtils;
import com.black.throwable.IOSException;
import lombok.NonNull;

import java.io.*;

public class IoUtils {

    public static String getThrowableMessage(Throwable e){
        if (e == null){
            return "no throwable";
        }
        String message = e.getMessage();
        if (!StringUtils.hasText(message)){
            return "no detailed exception information";
        }
        return message;
    }

    public static byte[] getBytes(Object source){
        return getBytes(source, false);
    }

    public static byte[] getBytes(Object source, boolean serialize){
        if (source == null){
            return ByteAllocator.alloc(0);
        }

        if (source instanceof byte[] || source instanceof Byte[]){
            return (byte[]) source;
        }

        if (source instanceof InputStream){
            try {
                return IoUtils.readBytes((InputStream) source);
            } catch (IOException e) {
                throw new IOSException(e);
            }
        }

        if (source instanceof ByteArrayOutputStream){
            return ((ByteArrayOutputStream) source).toByteArray();
        }

        if (source instanceof String){
            return ((String) source).getBytes();
        }

        if (serialize && source instanceof Serializable){
            try {
                return IoUtil.toBuffer(source);
            } catch (IOException e) {
                throw new IOSException(e);
            }
        }
        return source.toString().getBytes();
    }

    public static byte[] castToBytes(Object source){
        if (source == null) return new byte[0];
        if (source instanceof byte[])return (byte[]) source;
        return source.toString().getBytes();
    }

    public static String readUTF(InputStream in) throws IOException {
        DataByteBufferArrayInputStream inputStream = new DataByteBufferArrayInputStream(in);
        String utf = inputStream.readUTF();
        inputStream.close();
        return utf;
    }

    public static String read(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        byte b;
        while ((b = (byte) in.read()) != -1){
            builder.append(b);
        }
        return builder.toString();
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        return buffer;
    }

    public static void writeIn(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        int b;
        while ((b = in.read()) != -1){
            out.write(b);
        }
        in.close();
    }

    public static byte[] read(InputStream in, byte[] buffer) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int size = in.read(buffer);
        if (size == -1){
            throw new IOException("read -1");
        }

        while (size == buffer.length){
            out.write(buffer);
            size = in.read(buffer);
            if (size == -1){
                throw new IOException("read -1");
            }
        }
        out.write(buffer, 0, size);
        return out.toByteArray();
    }

}
