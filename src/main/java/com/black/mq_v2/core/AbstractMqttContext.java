package com.black.mq_v2.core;

import com.black.mq_v2.MQTTException;
import com.black.mq_v2.MqttUtils;
import com.black.mq_v2.definition.MqttContext;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.utils.ServiceUtils;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public abstract class AbstractMqttContext implements MqttContext {

    protected Object source;

    protected String serverHost;

    protected int serverPort;

    protected String serverUrl;

    protected String userName;

    protected String password;

    protected IoLog log;

    protected volatile boolean async = false;

    protected volatile boolean connectState = false;

    protected String name;

    public AbstractMqttContext(){
        this(MqttUtils.createName());
    }

    public AbstractMqttContext(String name){
        log = createLog();
        this.name = name;
    }

    protected abstract Object buildSource();

    protected abstract void doConnect() throws Throwable;

    protected IoLog createLog(){
        return LogFactory.getArrayLog();
    }

    @Override
    public void createSource() {
        buildSource();
    }

    @Override
    public void connect() {
        if (source == null){
            source = buildSource();
        }
        try {
            doConnect();
        } catch (Throwable e) {
            log.error("connect mqtt server fair -- {}",
                    ServiceUtils.getThrowableMessage(e, "unknown"));
            throw new MQTTException(e);
        }
        connectState = true;
    }

    @Override
    public void setServerHostAndPort(String host, int port) {
        setServerHost(host);
        setServerPort(port);
    }

    @Override
    public Object source() {
        return source;
    }

    protected abstract void close0();

    @Override
    public void close() {
        if (source != null && isConnectState()){
            close0();
        }
    }
}
