package com.black.core.sql.code.packet;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.pattern.ExecuteBody;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResultPacket {

    private final ExecutePacket ep;

    private final Configuration configuration;

    private ExecuteBody executeBody;

    private Object result;

    public ResultPacket(ExecutePacket ep) {
        this.ep = ep;
        configuration = ep.getConfiguration();
    }
}
