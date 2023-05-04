package com.black.share;

import com.black.core.util.StreamUtils;
import com.black.function.Function;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.JHexByteArrayOutputStream;
import com.black.utils.IdUtils;
import com.black.utils.JHex;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@SuppressWarnings("all")
public class ShareUtils {

    public static final Function<Byte, Boolean> headFunction = h -> {
        return "FF".equalsIgnoreCase(JHex.encodeString(new byte[]{h}));
    };

    public static final Function<Byte, Boolean> tailFunction = h -> {
        return "0F".equalsIgnoreCase(JHex.encodeString(new byte[]{h}));
    };

    /*
        协议格式
        请求:
        FF: 包头
        714D4772694C6430 16位 8个字节 request id
        01 请求目的  1: 执行方法
        utf: 方法名称
        0x: 参数个数
        n*utf 参数列表
        0F: 包尾

        响应:
        FF:包头
        714D4772694C6430 16位 8个字节 response id
        02:正常响应
        utf: result
        0F: 包尾

        03: 异常
        urf 异常信息
        0F:包尾
     */

    public static byte[] createInvokeMethodPackage(String requestId, String methodName, Object... params){
        InvokeMethodRequest invokeMethodRequest = new InvokeMethodRequest(methodName, params);
        invokeMethodRequest.setRequestId(requestId);
        JHexByteArrayOutputStream out = new JHexByteArrayOutputStream();
        try {
            out.writeHexString("FF");
            out.writeHexJavaObject(invokeMethodRequest);
            out.writeHexString("0F");
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<Request> resolveRequest(byte[] bytes){
        JHexByteArrayInputStream in = new JHexByteArrayInputStream(bytes);
        try {
            List<JHexByteArrayInputStream> list = in.unpacking(headFunction, tailFunction);
            return StreamUtils.mapList(list, packIn -> {
                try {
                    return (Request) packIn.readHexJavaObject();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] createResponseBytes(Response response){
        JHexByteArrayOutputStream out = new JHexByteArrayOutputStream();
        try {
            out.writeHexString("FF");
            out.writeHexJavaObject(response);
            out.writeHexString("0F");
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public static byte[] createNormalReturn(String requestId, Object result){
        NormalResponse response = new NormalResponse(result);
        response.setResponseId(requestId);
        JHexByteArrayOutputStream out = new JHexByteArrayOutputStream();
        try {
            out.writeHexString("FF");
            out.writeHexJavaObject(response);
            out.writeHexString("0F");
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] createNormalReturn(String requestId, Throwable throwable){
        ThrowableResponse response = new ThrowableResponse(throwable);
        response.setResponseId(requestId);
        JHexByteArrayOutputStream out = new JHexByteArrayOutputStream();
        try {
            out.writeHexString("FF");
            out.writeHexJavaObject(response);
            out.writeHexString("0F");
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<Response> resolveResponse(byte[] bytes){
        JHexByteArrayInputStream in = new JHexByteArrayInputStream(bytes);
        try {
            List<JHexByteArrayInputStream> list = in.unpacking(headFunction, tailFunction);
            return StreamUtils.mapList(list, packIn -> {
                try {
                    return (Response) packIn.readHexJavaObject();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }



    public static void main(String[] args) {
        System.out.println(JHex.encodeObject(IdUtils.createShort8Id()));
    }

}
