package com.black.core.sql.code.inter;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;

public interface ConfigurationAnnotationResolver {


    void doReolver(Configuration configuration, ExecutePacket ep);


}
