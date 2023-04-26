package com.black.rpc.socket;

import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.nio.code.ChannelHandler;
import com.black.nio.code.ChannelHandlerContext;
import com.black.nio.code.NioChannel;
import com.black.rpc.RpcConfiguration;
import com.black.rpc.RpcMessageResolver;
import com.black.rpc.RpcRequestType;
import com.black.rpc.RpcUtils;
import com.black.rpc.ill.CreateRequestException;
import com.black.rpc.inter.RequestDeserializer;
import com.black.rpc.log.Log;
import com.black.rpc.request.PrepareRequest;
import com.black.rpc.request.Request;
import com.black.utils.IoUtils;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.black.rpc.RpcUtils.isComplete;

public class NioServerSocketTemplate implements ChannelHandler {

    public static AtomicInteger requestCount = new AtomicInteger();

    final RpcConfiguration configuration;

    final ThreadLocal<PrepareRequest> incompleteRequestLocal = new ThreadLocal<>();

    public NioServerSocketTemplate(RpcConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void error(ChannelHandlerContext chc, Throwable e) throws IOException {
        Log log = configuration.getLog();
        log.error("与客户端交互发生 io 异常: {}, 即将关闭连接", e.getMessage());
        chc.channel().close();
    }

    @Override
    public void acceptComplete(ChannelHandlerContext chc) {
        Log log = configuration.getLog();
        log.info("与客户端完成连接: {}", chc.channel().nameAddress());
        ChannelHandler.super.acceptComplete(chc);
    }

    @Override
    public void flush(ChannelHandlerContext chc) {
        ChannelHandler.super.flush(chc);
    }

    @Override
    public void write(ChannelHandlerContext chc, Object source) throws IOException {
        Log log = configuration.getLog();
        log.debug("发送响应(字节数):[{}] -- 到客户端: [{}]", IoUtils.castToBytes(source).length, chc.channel().nameAddress());
        ChannelHandler.super.write(chc, source);
    }

    @Override
    public void close(ChannelHandlerContext chc) {
        Log log = configuration.getLog();
        log.error("与客户端断开连接: {}", chc.channel().nameAddress());
        ChannelHandler.super.close(chc);
    }

    @Override
    public void read(ChannelHandlerContext chc, Object source) throws IOException {
        Log log = configuration.getLog();
        byte[] bytes = IoUtils.castToBytes(source);
        System.out.println("rece bytes len:" + bytes.length);
        NioChannel nioChannel = chc.channel();
        PrepareRequest incompleteRequest = waitRemainingData();
        if (incompleteRequest != null){
            log.debug("拼接后续请求: [{}]", incompleteRequest.getRequestId());
            if (splicingRequest(incompleteRequest, bytes)) {
                executeRequest(nioChannel, incompleteRequest);
                incompleteRequestLocal.remove();
            }
        }else {
            PrepareRequest prepareRequest = RpcUtils.deserializePrepareRequest(new DataByteBufferArrayInputStream(bytes));
            if (isComplete(prepareRequest)){
                executeRequest(nioChannel, prepareRequest);
            }else {
                log.debug("请求不完整, 等待后续请求: [{}]", prepareRequest.getRequestId());
                incompleteRequestLocal.set(prepareRequest);
            }
        }
    }

    private PrepareRequest waitRemainingData(){
        return incompleteRequestLocal.get();
    }

    private boolean splicingRequest(PrepareRequest incompleteRequest, byte[] bytes) throws IOException {
        incompleteRequest.appendBody(bytes);
        return incompleteRequest.getBodySize() == incompleteRequest.bodyBufferSize();
    }

    private void executeRequest(NioChannel nioChannel, PrepareRequest prepareRequest){
        Log log = configuration.getLog();
        nioChannel.executeWork(() -> {
            Request request = null;
            try {
                request = deserializerRequest(prepareRequest);
            } catch (CreateRequestException e) {
                //不做响应
            }
            if (request == null){
                return;
            }
            log.info("处理请求: [{}] [{}]", request.getRequestId(), requestCount.incrementAndGet());
            RpcMessageResolver<Request> messageResolver = (RpcMessageResolver<Request>) configuration.getRpcMessageResolver();
            messageResolver.resolve(request, new NioRemoteSocket(nioChannel));
        });
    }


    private Request deserializerRequest(PrepareRequest prepareRequest) throws CreateRequestException {
        Log log = configuration.getLog();
        int typeInt = prepareRequest.getType();
        Request request = null;
        try {

            RpcRequestType requestType = RpcRequestType.typeOf(typeInt);
            LinkedBlockingQueue<RequestDeserializer> requestDeserializers = configuration.getRequestDeserializers();
            for (RequestDeserializer deserializer : requestDeserializers) {
                if (deserializer.support(requestType)) {
                    request = deserializer.deserializeRequest(prepareRequest);
                    break;
                }
            }
        }catch (Throwable e){
            log.error("反序列化请求错误");
            throw new CreateRequestException("error: deserialization request", e);
        }

        if (request == null){
            log.error("无法处理的请求类型: {}", typeInt);
            throw new CreateRequestException("the request type cannot be supported");
        }
        log.info("请求创建完成: [{}]", request);
        return request;
    }
}
