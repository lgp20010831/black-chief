package com.black.core.sql.code.inter;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.pattern.ExecuteBody;

public interface PointSessionExecutor {

    boolean support(Configuration configuration);

    ExecuteBody doExecute(ExecutePacket ep);
}
