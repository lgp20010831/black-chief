package com.black.http.rpc;

import com.black.function.Consumer;
import com.black.http.Configuration;
import com.black.http.HttpDispatcher;
import com.black.nio.group.NioType;
import com.black.nio.group.Session;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HttpRpcContext {

    private String httpHost = "0.0.0.0";

    private int httpPort = 8000;

    private String remoteHost = "0.0.0.0";

    private int remotePort = 7000;

    private NioType type = NioType.CHIEF;

    private Consumer<HttpDispatcher> httpDispatcherCallback;

    public Session open(){
        try {
            HttpDispatcher dispatcher = buildDispatcher();
            Configuration configuration = dispatcher.getConfiguration();
            configuration.setTransitAgreement(new RpcTransitAgreement());
            return dispatcher.open();
        } catch (Throwable e) {
            throw new IllegalStateException("open dispatcher fail", e);
        }
    }


    private HttpDispatcher buildDispatcher() throws Throwable {
        Configuration configuration = new Configuration();
        configuration.setHttpHost(httpHost);
        configuration.setHttpPort(httpPort);
        configuration.setRemoteHost(remoteHost);
        configuration.setRemotePort(remotePort);
        HttpDispatcher dispatcher = new HttpDispatcher(configuration, type);
        if (httpDispatcherCallback != null){
            httpDispatcherCallback.accept(dispatcher);
        }
       return dispatcher;
    }

}
