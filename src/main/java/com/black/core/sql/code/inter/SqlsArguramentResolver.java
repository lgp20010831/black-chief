package com.black.core.sql.code.inter;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;

public interface SqlsArguramentResolver {

    boolean support(Configuration configuration, ParameterWrapper pw);

    void doResolver(Configuration configuration, ExecutePacket ep, Object value, ParameterWrapper pw);
}
