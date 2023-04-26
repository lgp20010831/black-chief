package com.black.mq;

import com.alibaba.fastjson.JSONObject;
import com.black.function.Function;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.JHexByteArrayOutputStream;
import com.black.mq.simple.DataType;
import com.black.mq.simple.Message;
import com.black.mq.simple.SimpleMessage;
import com.black.core.util.Av0;
import com.black.utils.JHex;


import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.black.utils.JHex.castStringToDigits;

//协议格式 6f(协议头) xx(消息等级 01不需要回复确认 00 需要回复确认) message id (36个字节, 16进制)     2d(协议尾)
public class MQUtils {


    public static void main(String[] args) throws IOException {
        JSONObject json = Av0.js("name", "lgp");
        JHexByteArrayOutputStream out = new JHexByteArrayOutputStream();
        out.writeHexObject(UUID.randomUUID());
        JHexByteArrayInputStream in = out.getInputStream();
        System.out.println(in.available());
        Object javaObject = in.readHexObjectString(in.available());
        System.out.println(javaObject);
    }



    public static final Function<Byte, Boolean> f = b -> {
        return DataType.START_16.equals(JHex.castStringToDigits(b));
    };

    public static void startOut(JHexByteArrayOutputStream out) throws IOException {
        out.writeHexString(DataType.START_16);
    }

    public static void endOut(JHexByteArrayOutputStream out) throws IOException {
        out.writeHexString(DataType.END_16);
        out.flush();
    }

    public static void startIn(JHexByteArrayInputStream in) throws IOException {
        String hex = in.readHexSting();
        if (!hex.equals(DataType.START_16)){
            throw new IOException("unrecognized packet id: " + hex);
        }
    }

    public static void endIn(JHexByteArrayInputStream in) throws IOException {
        String hex = in.readHexSting();
        if (!hex.equals(DataType.END_16)){
            throw new IOException("unrecognized packet id: " + hex);
        }
    }
    public static Message copyMessage(Message message){
        return new SimpleMessage(message.content(), message.level());
    }

    public static boolean isRequired(Message message){
        return message.level() == 0;
    }

    public static List<JHexByteArrayInputStream> unpack(JHexByteArrayInputStream inputStream) throws IOException {
        return inputStream.unpacking(f, f);
    }
}
