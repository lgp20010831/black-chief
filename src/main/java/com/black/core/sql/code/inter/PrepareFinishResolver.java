package com.black.core.sql.code.inter;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.sql.SqlOutStatement;


public interface PrepareFinishResolver {

    boolean support(Configuration configuration);

    //返回拦截状态 true 标识拦截
    boolean handler(Configuration configuration, ExecutePacket ep, SqlOutStatement statement);
}
