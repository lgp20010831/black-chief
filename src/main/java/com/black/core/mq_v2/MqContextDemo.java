package com.black.core.mq_v2;

import com.black.mq.server.NioChiefMQServer;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.StopRun;

@StopRun
public class MqContextDemo implements OpenComponent {
    NioChiefMQServer server;
    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) throws Throwable {
//        server = new NioChiefMQServer();
//        server.bind("0.0.0.0", 1000);
//        MqttContextRegister register = MqttContextRegister.getInstance();
//        MqttContext context = MqttDiverManager.load(MqttDriver.CHIEF);
//        context.setServerHostAndPort("0.0.0.0", 1000);
//        context.createSource();
//        register.registerContext(context);
    }
}
