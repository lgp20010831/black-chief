package com.black.share;


import com.black.io.out.JHexByteArrayOutputStream;
import com.black.nio.group.*;
import com.black.utils.IdUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public abstract class ShareClient implements GioResolver, MethodInvoker {

    protected final String host;

    protected final int port;

    protected final RemoteDecisionMaker remoteDecisionMaker;

    protected Session session;

    protected static long waitTime = 5000;

    protected final Map<String, Thread> waitThreadMap = new ConcurrentHashMap<>();

    protected final Map<String, Object> resultMap = new LinkedHashMap<>();

    protected ShareClient(String host, int port, RemoteDecisionMaker remoteDecisionMaker) {
        this.host = host;
        this.port = port;
        this.remoteDecisionMaker = remoteDecisionMaker;
        connectServer();
    }


    protected void connectServer(){
        session = Factorys.open(this, NioType.JHEX)
                .apply(configuration -> {
                    configuration.setHost(host);
                    configuration.setPort(port);
                })
                .openSession();
    }

    @Override
    public void read(GioContext context, byte[] bytes, JHexByteArrayOutputStream out) throws IOException {
        List<Response> responses = ShareUtils.resolveResponse(bytes);
        for (Response response : responses) {
            resolveResponse(response);
        }
    }

    protected void resolveCulitivateBranchResponse(CulitivateBranchResponse culitivateBranchResponse){
        Object process = culitivateBranchResponse.getProcess();
        int port = culitivateBranchResponse.getPort();
        System.out.println("成为新的 leader: " + port);
        remoteDecisionMaker.setShareServer(remoteDecisionMaker.openServer("0.0.0.0", port));
        remoteDecisionMaker.getShareServer().reset(process);
        remoteDecisionMaker.setShareClient(null);
    }

    protected void resolveCutBranchResponse(CutBranchResponse cutBranchResponse){
        String host = cutBranchResponse.getHost();
        int port = cutBranchResponse.getPort();
        remoteDecisionMaker.setShareClient(remoteDecisionMaker.openClient(host, port));
        remoteDecisionMaker.setShareServer(null);
    }

    protected void resolveResponse(Response response){
        String responseId = response.getResponseId();
        Object result = null;
        if (response instanceof NormalResponse){
            result = ((NormalResponse) response).getResult();
        }else if (response instanceof ThrowableResponse){
            result = ((ThrowableResponse) response).getThrowable();
        }else if (response instanceof CulitivateBranchResponse){
            resolveCulitivateBranchResponse((CulitivateBranchResponse) response);
            return;
        }else if (response instanceof CutBranchResponse){
            resolveCutBranchResponse((CutBranchResponse) response);
            return;
        }

        resultMap.put(responseId, result);
        Thread thread = waitThreadMap.get(responseId);
        if (thread != null){
            thread.interrupt();
        }
    }

    public Object invokeMethod(String methodName, Object... params) throws Throwable{
        String requestId = IdUtils.createShort8Id();
        byte[] bytes = ShareUtils.createInvokeMethodPackage(requestId, methodName, params);
        session.writeAndFlushAsSocket(bytes);
        return waitResult(requestId);
    }

    protected Object waitResult(String id) throws Throwable {
        Thread thread = Thread.currentThread();
        waitThreadMap.put(id, thread);
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.interrupted();
            waitThreadMap.remove(id);
            Object result = resultMap.remove(id);
            if (result instanceof Throwable){
                throw (Throwable) result;
            }else {
                return result;
            }
        }
        return null;
    }


}
