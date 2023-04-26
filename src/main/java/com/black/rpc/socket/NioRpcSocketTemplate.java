package com.black.rpc.socket;

import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.nio.code.*;
import com.black.rpc.*;
import com.black.rpc.log.Log;
import com.black.rpc.request.Request;
import com.black.rpc.response.PrepareResponse;
import com.black.rpc.response.Response;
import com.black.utils.IoUtils;

import java.io.IOException;

public class NioRpcSocketTemplate implements ChannelHandler {

    final RpcConfiguration configuration;

    final ThreadLocal<PrepareResponse> incompleteResponseLocal = new ThreadLocal<>();

    public NioRpcSocketTemplate(RpcConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void error(ChannelHandlerContext chc, Throwable e) throws IOException {
        chc.channel().close();
        Log log = configuration.getLog();
        log.error("与服务端交互发生错误: {}, 即将尝试重连", e.getMessage());
        log.error(e);
    }

    @Override
    public void write(ChannelHandlerContext chc, Object source) throws IOException {
        Log log = configuration.getLog();
        log.debug("发送请求到服务器, 字节数:[{}]", IoUtils.castToBytes(source).length);
        ChannelHandler.super.write(chc, source);
    }

    @Override
    public void read(ChannelHandlerContext chc, Object source) throws IOException {
        Log log = configuration.getLog();
        byte[] bytes = IoUtils.castToBytes(source);
        NioChannel nioChannel = chc.channel();
        System.out.println("rece bytes len:" + bytes.length);
        PrepareResponse waitResponse = waitResponse();
        if (waitResponse != null){
            log.debug("拼接后续响应:[{}]", waitResponse.getRequestId());
            if (splicingResponse(waitResponse, bytes)){
                executeResponse(nioChannel, waitResponse.toResponse());
                incompleteResponseLocal.remove();
            }
        }else {
            DataByteBufferArrayInputStream in = new DataByteBufferArrayInputStream(bytes);
            PrepareResponse prepareResponse = RpcUtils.deserializePrepareResponse(in);
            if (RpcUtils.isComplete(prepareResponse)){
                executeResponse(nioChannel, prepareResponse.toResponse());
            }else {
                log.debug("响应不完整, 等待后续请求:[{}]", prepareResponse.getRequestId());
                incompleteResponseLocal.set(prepareResponse);
            }
        }
    }

    private boolean isComplete(Request request){
        int sendSize = request.getBodySize();
        int utfBytesLen = DataByteBufferArrayOutputStream.getUtfBytesLen(request.getParam());
        return sendSize == utfBytesLen;
    }

    private PrepareResponse waitResponse(){
        return incompleteResponseLocal.get();
    }

    private boolean splicingResponse(PrepareResponse response, byte[] bytes) throws IOException {
        response.appendBody(bytes);
        return response.getBodySize() <= response.bodyBufferSize();
    }

    private void executeResponse(NioChannel nioChannel, Response response){
        Log log = configuration.getLog();
        log.info("处理响应: {}", response);
        nioChannel.executeWork(() -> {
            RpcMessageResolver messageResolver = configuration.getRpcMessageResolver();
            messageResolver.resolve(response, new NioRemoteSocket(nioChannel));
        });
    }

    @Override
    public void connectComplete(ChannelHandlerContext chc) {
        Log log = configuration.getLog();
        log.info("与服务端连接完成");
        ChannelHandler.super.connectComplete(chc);
    }

    @Override
    public void close(ChannelHandlerContext chc) {
        Log log = configuration.getLog();
        log.error("与服务端连接断开");
        ChannelHandler.super.close(chc);
    }
}
