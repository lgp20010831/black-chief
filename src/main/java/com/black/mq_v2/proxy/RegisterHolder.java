package com.black.mq_v2.proxy;

import com.black.mq_v2.definition.MqttContext;
import com.black.core.util.StringUtils;
import lombok.Getter;

@Getter
public class RegisterHolder {

    private final MqttContext context;

    private final MessageSendProxyRegister sendProxyRegister;

    private final MessageArrivedMethodRegister arrivedMethodRegister;

    public RegisterHolder(MqttContext context) {
        this.context = context;
        arrivedMethodRegister = new MessageArrivedMethodRegister(context);
        sendProxyRegister = new MessageSendProxyRegister(context);
    }


    @Override
    public String toString() {
        return StringUtils.letString("-------------", context.getName(), "-------------", "\n",
                sendProxyRegister, "\n",
                arrivedMethodRegister, "\n",
                "-------------", context.getName(), "-------------");
    }
}
