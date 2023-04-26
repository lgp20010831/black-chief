package com.black.core.sql.code.pattern;

import com.black.pattern.Pipeline;
import com.black.core.sql.code.node.Head;
import com.black.core.sql.code.node.Tail;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;

import java.util.Collection;
import java.util.HashSet;

public class PipelinesManager {


    private final static ThreadLocal<Pipeline<AbstractSqlsPipeNode, ExecutePacket, ResultPacket>> lineModel = new ThreadLocal<>();

    private final static Collection<PipelineHook<AbstractSqlsPipeNode, ExecutePacket, ResultPacket>> hooks = new HashSet<>();

    public static void registerHook(PipelineHook<AbstractSqlsPipeNode, ExecutePacket, ResultPacket> hook){
        if (hook != null){
            hooks.add(hook);
        }
    }

    public static Pipeline<AbstractSqlsPipeNode, ExecutePacket, ResultPacket> getCurrentPipeline(){
        Pipeline<AbstractSqlsPipeNode, ExecutePacket, ResultPacket> pipeline = lineModel.get();
        if (pipeline == null){
            pipeline = new Pipeline<>(new Head(), new Tail());
            for (PipelineHook<AbstractSqlsPipeNode, ExecutePacket, ResultPacket> hook : hooks) {
                hook.callback(pipeline);
            }
            lineModel.set(pipeline);
        }
        return pipeline;
    }

}
