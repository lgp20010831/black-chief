package com.black.http;

import com.black.core.util.StringUtils;
import com.black.utils.IoUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestUtils {


    public static Request parseRequest(Object source) throws IOException {
        byte[] bytes = IoUtils.getBytes(source, false);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        Request request = new Request();
        String line = reader.readLine();
        String[] strs = StringUtils.split(line, " ", 3, "read first line error:" + line);
        request.setMethod(strs[0]);
        request.setUri(strs[1]);
        request.setVersion(strs[2]);
        decodeRequestHeader(reader, request);
        decodeRequestMessage(reader, request);
        return request;
    }

    private static void decodeRequestHeader(BufferedReader reader, Request request) throws IOException {
        Map<String, String> headers = new HashMap<>(16);
        String line = reader.readLine();
        String[] kv;
        while (!"".equals(line)) {
            kv = StringUtils.split(line, ":");
            headers.put(kv[0].trim(), kv[1].trim());
            line = reader.readLine();
        }
        request.setHeaders(headers);
    }

    private static void decodeRequestMessage(BufferedReader reader, Request request) throws IOException {
        int contentLen = Integer.parseInt(request.getHeaders().getOrDefault("Content-Length", "0"));
        // 表示没有message，直接返回
        // 如get/options请求就没有message
        if (contentLen == 0) {
            return;
        }
        char[] message = new char[contentLen];
        reader.read(message);
        request.setMessage(new String(message));
    }



}
