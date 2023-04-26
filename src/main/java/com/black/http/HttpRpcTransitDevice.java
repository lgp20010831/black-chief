package com.black.http;

import com.alibaba.fastjson.JSONObject;
import com.black.rpc.RpcFormat;
import com.black.rpc.RpcRequestType;
import com.black.rpc.request.CommonRequest;
import com.black.rpc.request.JsonRequest;
import com.black.rpc.response.Response;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;
import com.black.utils.IdUtils;
import com.black.utils.IoUtils;

import java.io.IOException;
import java.util.Map;

public class HttpRpcTransitDevice {

    public static byte[] castHttpRequestToRpcRequest(byte[] bytes) throws IOException {
        Request request = HttpRequestUtils.parseRequest(bytes);
        return castHttpRequestToRpcRequest0(request);
    }

    public static byte[] castHttpRequestToRpcRequest0(Request request) throws IOException{
        Map<String, String> headers = request.getHeaders();
        String contentType = headers.get("Content-Type");
        String requestId = IdUtils.createId();
        String servletPath = request.getServletPath();
        String method = request.getMethod();
        com.black.rpc.request.Request rpcRequest;
        if ("application/json".equals(contentType)){
            rpcRequest = new JsonRequest();
            reflexSetValue(rpcRequest, "requestType", RpcRequestType.JSON);
        }else {
            rpcRequest = new CommonRequest();
            reflexSetValue(rpcRequest, "requestType", RpcRequestType.COMMON);
        }
        reflexSetValue(rpcRequest, "requestId", requestId);
        reflexSetValue(rpcRequest, "methodName", servletPath);
        JSONObject paramMap = request.getUrlParamJson();
        if ("GET".equalsIgnoreCase(method)){
            rpcRequest.addParam(paramMap.toString());
        }else if ("POST".equalsIgnoreCase(method)){
            JSONObject json = new JSONObject();
            json.put("urlParam", paramMap.toJSONString());
            json.put("body", request.getMethod());
            rpcRequest.addParam(json.toString());
        }

        return RpcFormat.createRequestBytes(rpcRequest);
    }


    public static byte[] castRpcResponseToHttpResponse(byte[] bytes) throws IOException {
        return IoUtils.getBytes(HttpResponseUtils.toResponseString(castRpcResponseToHttpResponse2(bytes)), false);
    }

    public static com.black.http.Response castRpcResponseToHttpResponse2(byte[] bytes) throws IOException {
        Response response = RpcFormat.castToResponse(bytes);
        int code;
        String msg;
        switch (response.getState()){
            case OK:
                code = 200;
                msg = "ok";
                break;
            case ERROR:
                code = 500;
                msg = "server error";
                break;
            case NO_METHOD:
                code = 404;
                msg = "not find method";
                break;
            case REQUEST_ERROR:
                code = 400;
                msg = "request invaild";
                break;
            default:
                throw new IllegalStateException("ill state");
        }
        return HttpResponseUtils.createResponse(String.valueOf(response.getParam()), code, msg, "application/json");
    }


    public static void reflexSetValue(Object bean, String name, Object value){
        ClassWrapper<?> wrapper = BeanUtil.getPrimordialClassWrapper(bean);
        FieldWrapper field = wrapper.getField(name);
        if (field != null){
            SetGetUtils.invokeSetMethod(field.get(), value, bean);
        }
    }
}
