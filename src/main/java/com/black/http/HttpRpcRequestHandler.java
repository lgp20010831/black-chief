package com.black.http;

import com.black.http.service.HttpRequestHandler;

import java.io.IOException;

public class HttpRpcRequestHandler implements HttpRequestHandler {
    private final HttpDispatcher dispatcher;

    public HttpRpcRequestHandler(HttpDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public HttpRpcRequestHandler copy(){
        return new HttpRpcRequestHandler(dispatcher);
    }

    @Override
    public Response handle(Request request) throws Throwable {
        return resolveBytes(request);
    }

    private Response resolveBytes(Request request) throws Throwable{
        Configuration configuration = dispatcher.getConfiguration();
        HttpTransitAgreement transitAgreement = configuration.getTransitAgreement();

        byte[] requestBytes = transitAgreement.httpRequestToAim(request);
        byte[] responseBytes = sendRequest(requestBytes);
        return transitAgreement.aimResponseToHttp(responseBytes);
    }

    private byte[] sendRequest(byte[] requestBytes) throws IOException {
        for (HttpTransitExecutor executor : dispatcher.getExecutors()) {
            if (executor.support(dispatcher.getAimType())) {
                return executor.send(requestBytes, dispatcher.getConfiguration());
            }
        }
        return new byte[0];
    }
}
