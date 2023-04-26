package com.black.core.sql.code.inter;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;
import com.black.core.sql.code.pattern.AbstractSqlsPipeNode;

public interface AppearanceResolver {

    default boolean headSupport(Configuration configuration){return false;}

    default void doHeadAppearance(AbstractSqlsPipeNode node, Configuration configuration, ExecutePacket ep){}

    default boolean tailSupport(Configuration configuration){return false;}

    default void doTailAppearance(AbstractSqlsPipeNode node, Configuration configuration, ResultPacket rp){}
}
