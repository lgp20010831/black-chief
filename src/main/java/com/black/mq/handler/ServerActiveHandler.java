package com.black.mq.handler;

import com.black.mq.server.MQInputStream;
import com.black.mq.server.MQServer;
import com.black.mq.simple.ActivationPackage;
import com.black.mq.simple.DataType;

public class ServerActiveHandler implements MessageHandler{

    private final MQServer server;

    public ServerActiveHandler(MQServer server) {
        this.server = server;
    }

    @Override
    public boolean support(int type) {
        return type == DataType.ACTIVE || type == DataType.RE_ACTIVE;
    }

    @Override
    public void handler(MQInputStream in, int type) throws Throwable {
        ActivationPackage pack = (ActivationPackage) in.readHexJavaObject();
        String address = in.getAddress();
        if (type == DataType.ACTIVE){
            server.registerActiveClient(pack, address);
        }else {
            server.removeAcitveClient(address);
            server.registerActiveClient(pack, address);
        }
    }
}
