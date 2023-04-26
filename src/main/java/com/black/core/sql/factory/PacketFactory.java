package com.black.core.sql.factory;

import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.packet.ResultPacket;

import java.util.ArrayList;
import java.util.List;

public class PacketFactory {

    private static ThreadLocal<List<ExecutePacket>> executeCache = new ThreadLocal<>();

    private static ThreadLocal<List<ResultPacket>> resultCache = new ThreadLocal<>();

    private static final ThreadLocal<ExecutePacket> epLocal = new ThreadLocal<>();

    public static void remove(){
        epLocal.remove();
    }

    public static ExecutePacket getCurrentPacket(){
        return epLocal.get();
    }

    public static void replace(ExecutePacket executePacket){
        if (executePacket != null){
            epLocal.set(executePacket);
        }
    }

    public static ExecutePacket createPacket(Configuration configuration){
        List<ExecutePacket> list = init(executeCache);
        ExecutePacket packet = new ExecutePacket(configuration);
        if (configuration instanceof AppearanceConfiguration){
            ((AppearanceConfiguration) configuration).setEp(packet);
        }
        epLocal.set(packet);
        list.add(packet);
        return packet;
    }

    public static ResultPacket createResult(ExecutePacket packet){
        List<ResultPacket> list = init(resultCache);
        ResultPacket rp = packet.getRp();
        list.add(rp);
        return rp;
    }

    static <T> List<T> init(ThreadLocal<List<T>> local){
        List<T> list = local.get();
        if (list == null){
            list = new ArrayList<>();
            local.set(list);
        }
        return list;
    }

    public static void close(){
        List<ResultPacket> list = init(resultCache);
        for (ResultPacket packet : list) {
            packet.getExecuteBody().close();
        }
        list.clear();
        init(executeCache).clear();
        epLocal.remove();
    }
}
