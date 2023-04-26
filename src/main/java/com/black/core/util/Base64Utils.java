package com.black.core.util;

import com.black.core.aop.servlet.encryption.CiphertextOperationException;
import com.black.core.io.IoSerializer;
import com.black.core.io.ObjectSerializer;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Base64Utils {

    public static BASE64Decoder decoder = new BASE64Decoder();
    public static BASE64Encoder encoder = new BASE64Encoder();
    public static ObjectSerializer objectSerializer = new IoSerializer();

    public static byte[] primordialDecode(String body){
        try {
            return decoder.decodeBuffer(body);
        } catch (IOException e) {
            throw new CiphertextOperationException(e);
        }
    }

    public static String primordialEecode(byte[] bytes){
        try {
            return encoder.encode(bytes);
        } catch (Throwable e) {
            throw new CiphertextOperationException(e);
        }
    }

    public static String decode(String body){
        try {
            return new String(decoder.decodeBuffer(body));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encode(Object obj){
        if (obj == null){
            return null;
        }
        if (obj instanceof String){
            return encoder.encode(((String) obj).getBytes());
        }else if (obj instanceof Map || obj instanceof List){
            return encoder.encode(obj.toString().getBytes());
        }else {
            try {
                return encoder.encode(objectSerializer.writeObject(obj));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
