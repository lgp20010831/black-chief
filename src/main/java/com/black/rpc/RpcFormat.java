package com.black.rpc;

import com.alibaba.fastjson.JSONObject;
import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.io.in.JHexByteArrayInputStream;
import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.rpc.request.CommonRequest;
import com.black.rpc.request.Request;
import com.black.rpc.response.CommonResponse;
import com.black.rpc.response.Response;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.MapArgHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

//rpc 协议报文格式
//编码格式固定为 utf
// 0000 0000000000000000 [0000000000000000][0000000000000000 |  0000..........]
// type requestId           method id          method name        method param
//响应格式:
//0000 0000000000000000 0000    [0 0000000000000000|0000000.........|0000000........]
//type requestId         state      bool method id | method result     error info...
public class RpcFormat {

    public static Response deserializeResponse(DataByteBufferArrayInputStream in) throws IOException {
        CommonResponse commonResponse = new CommonResponse();
        int type = in.readInt();
        commonResponse.setRequestType(RpcRequestType.typeOf(type));
        String requestId = readRequestId(in);
        commonResponse.setRequestId(requestId);
        int state = in.readInt();
        commonResponse.setState(RpcState.typeOf(state));
        commonResponse.setBodySize(in.readInt());
        commonResponse.setBody(in.readUnrestrictedUtf());
        return commonResponse;
    }

    public static String readRequestMethod(DataByteBufferArrayInputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        char c;
        while ((c = in.readChar()) != '^'){
            builder.append(c);
        }
        return builder.toString();
    }

    public static String readRequestId(DataByteBufferArrayInputStream in) throws IOException {
        return in.readUTF();
    }


    public static Response createOkResponse(Request request, Object result){
        CommonResponse commonResponse = new CommonResponse();
        commonResponse.setBody(result);
        commonResponse.setState(RpcState.OK);
        commonResponse.setRequestId(request.getRequestId());
        commonResponse.setBodySize(DataByteBufferArrayOutputStream.getUtfBytesLen(result));
        commonResponse.setRequestType(request.getRequestType());
        return commonResponse;
    }

    public static Response castToResponse(byte[] bytes) throws IOException {
        JHexByteArrayInputStream inputStream = new JHexByteArrayInputStream(bytes);
        int i = inputStream.readInt();
        CommonResponse response = new CommonResponse();
        response.setRequestType(RpcRequestType.typeOf(i));
        response.setRequestId(inputStream.readUTF());
        response.setState(RpcState.typeOf(inputStream.readInt()));
        response.setBodySize(inputStream.readInt());
        response.setBody(inputStream.readUnrestrictedUtf());
        return response;
    }

    public static byte[] createRequestBytes(Request request) throws IOException {
        DataByteBufferArrayOutputStream out = new DataByteBufferArrayOutputStream();
        out.writeInt(request.getRequestType().getType());
        out.writeUTF(request.getRequestId());
        for (char c : request.getMethodName().toCharArray()) {
            out.writeChar(c);
        }
        out.writeChar('^');
        out.writeInt(request.getBodySize());
        Object param = request.getParam();
        out.writeUnrestrictedUtf(param == null ? "" : param.toString());
        return out.toByteArray();
    }

    public static Request createCommonRequest(MethodWrapper mw, Object[] args) throws IOException {
        CommonRequest commonRequest = new CommonRequest();
        commonRequest.setRequestType(RpcRequestType.COMMON);
        commonRequest.setRequestId(UUID.randomUUID().toString());
        commonRequest.setMethodName(mw.getName());
        String paramBody;
        if (args.length == 1){
            Object arg = args[0];
            paramBody = arg == null ? "" : arg.toString();
        }else if (args.length == 0){
            paramBody = "";
        }else {
            Map<String, Object> map = MapArgHandler.parse(args, mw);
            paramBody = new JSONObject(map).toString();
        }
        commonRequest.setActuatorParamBody(paramBody);
        return commonRequest;
    }
}
