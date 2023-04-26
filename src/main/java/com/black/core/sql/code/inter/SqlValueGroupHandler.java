package com.black.core.sql.code.inter;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.sqls.SqlValueGroup;

import java.util.List;

public interface SqlValueGroupHandler {

    boolean support(Configuration configuration);

    List<SqlValueGroup> handler(Configuration configuration, ExecutePacket ep);
}
