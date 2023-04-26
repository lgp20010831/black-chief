package com.black.treaty.rtp;

import com.black.nio.group.Session;
import com.black.treaty.TreatyBuilder;
import com.black.treaty.TreatyConfig;

public class RtpApplication {

    private final RtpConfig config;


    public RtpApplication(RtpConfig config) {
        this.config = config;
    }

    public RtpConfig getConfig() {
        return config;
    }

    public Session run(){
        TreatyConfig treatyConfig = new TreatyConfig();
        treatyConfig.setBindHost(config.getBindHost());
        treatyConfig.setBindPort(config.getBindPort());
        treatyConfig.setIoThreadNum(config.getIoThreadNum());
        treatyConfig.setNioType(config.getNioType());
        treatyConfig.setTreatyCustomHandler(new RtpTreatyHandler(this));
        return TreatyBuilder.base(treatyConfig).bind();
    }
}
