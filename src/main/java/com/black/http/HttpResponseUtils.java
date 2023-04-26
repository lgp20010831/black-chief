package com.black.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponseUtils {

    public static Response ok(String response, ContentType contentType){
        return createResponse(response, 200, "successful", contentType.toString());
    }

    public static Response fair(String response, ContentType contentType){
        return createResponse(response, 500, "system error", contentType.toString());
    }

    public static Response createResponse(String response, int code, String state, String contentType){
        Response httpResponse = new Response();
        httpResponse.setCode(code);
        httpResponse.setStatus(state);
        httpResponse.setVersion("HTTP/1.1");
        Map<String, String> headers = new HashMap<>();
        //headers.put("Content-Type", "application/json");
        headers.put("Content-Type", contentType);
        headers.put("Content-Length", String.valueOf(response.getBytes().length));
        httpResponse.setHeaders(headers);
        httpResponse.setMessage(response);
        return httpResponse;
    }

    public static String toResponseString(Response response){
        StringBuilder builder = new StringBuilder();
        buildResponseLine(response, builder);
        buildResponseHeaders(response, builder);
        buildResponseMessage(response, builder);
        return builder.toString();
    }

    public static String buildResponse(String response, int code, String state){
        return buildResponse(response, code, state, "text/html");
    }

    public static String buildResponse(String response, int code, String state, String contentType) {
        Response httpResponse = createResponse(response, code, state, contentType);
        return toResponseString(httpResponse);
    }

    private static void buildResponseLine(Response response, StringBuilder stringBuilder) {
        stringBuilder.append(response.getVersion()).append(" ").append(response.getCode()).append(" ")
                .append(response.getStatus()).append("\n");
    }

    private static void buildResponseHeaders(Response response, StringBuilder stringBuilder) {
        for (Map.Entry entry : response.getHeaders().entrySet()) {
            stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        stringBuilder.append("\n");
    }

    private static void buildResponseMessage(Response response, StringBuilder stringBuilder) {
        stringBuilder.append(response.getMessage());
    }

}
