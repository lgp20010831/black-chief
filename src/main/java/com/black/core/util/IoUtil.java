package com.black.core.util;

import com.black.core.io.IoSerializer;
import com.black.core.io.ObjectSerializer;
import lombok.NonNull;

import java.io.*;

public class IoUtil {

    private static final ObjectSerializer objectSerializer = new IoSerializer();

    public static <T> T deepCopy(T target){
        return objectSerializer.copyObject(target);
    }

    public static byte[] toBuffer(Object bean) throws IOException {
        return objectSerializer.writeObject(bean);
    }

    public static Object serialize(byte[] buffer) throws IOException {
        return objectSerializer.readObject(buffer);
    }

    public static void writeIn(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        int b;
        while ((b = in.read()) != -1){
            out.write(b);
        }
        in.close();
    }

    public static void writeToFile(@NonNull String path, @NonNull byte[] buffer){
        try {
            FileOutputStream outputStream = new FileOutputStream(path);
            outputStream.write(buffer);
            outputStream.flush();
            outputStream.close();
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

}
