package com.black.core.sql.code.listener;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.SQLListener;
import com.black.core.sql.annotation.UpdateOrInsert;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.UpdateOrInsertConifg;
import com.black.core.sql.code.ill.StopSqlInvokeException;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.factory.PacketFactory;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.Assert;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SQLListener
public class UpdateOrInsertListener implements GlobalSQLRunningListener {

    private final ThreadLocal<Object> lock = new ThreadLocal<>();

    private final Map<Method, UpdateOrInsertConifg> conifgCache = new ConcurrentHashMap<>();

    private final UpdateMasterProcessor updateMasterProcessor;

    private final InsertMasterProcessor insertMasterProcessor;

    public UpdateOrInsertListener() {
        updateMasterProcessor = new UpdateMasterProcessor();
        insertMasterProcessor = new InsertMasterProcessor();
    }

    @Override
    public void afterProcessorOfBatch() {
        updateMasterProcessor.clearRetrieval();
    }

    @Override
    public boolean interceptBatchs(String sql, SqlValueGroup sqlValueGroup) {
        Object o = lock.get();
        if (o != null) return false;
        lock.set(new Object());
        try {
            ExecutePacket executePacket = PacketFactory.getCurrentPacket();
            Assert.notNull(executePacket, "ep is not exist in manager");
            Configuration configuration = executePacket.getConfiguration();
            MethodWrapper methodWrapper = configuration.getMethodWrapper();
            if (!conifgCache.containsKey(methodWrapper.getMethod())){
                if (!methodWrapper.hasAnnotation(UpdateOrInsert.class) ||
                        !(configuration.getMethodType() == SQLMethodType.INSERT ||
                                configuration.getMethodType() == SQLMethodType.UPDATE)){
                    return false;
                }
                UpdateOrInsert annotation = methodWrapper.getAnnotation(UpdateOrInsert.class);
                conifgCache.put(methodWrapper.getMethod(), AnnotationUtils.loadAttribute(annotation, new UpdateOrInsertConifg()));
            }
            boolean intercept = false;
            UpdateOrInsertConifg conifg = conifgCache.get(methodWrapper.getMethod());
            try {
                switch (configuration.getMethodType()){
                    case INSERT:
                        if (insertMasterProcessor.insertExist(sql, sqlValueGroup, configuration, conifg)){
                            intercept = true;
                        }
                        break;
                    case UPDATE:
                        if (!updateMasterProcessor.updateExist(sql, sqlValueGroup, configuration, conifg)) {
                            intercept = true;
                        }
                        break;
                }
            }catch (Throwable e){
                if (e instanceof StopSqlInvokeException){
                    System.out.println(e.getMessage());
                    return false;
                }
                throw new SQLSException(e);
            }
            return intercept;
        }finally {
            lock.remove();
        }
    }

    @Override
    public void intercptCallback(String sql, SqlValueGroup sqlValueGroup, ExecuteBody executeBody) {
        ExecutePacket executePacket = PacketFactory.getCurrentPacket();
        Assert.notNull(executePacket, "ep is not exist in manager");
        Configuration configuration = executePacket.getConfiguration();
        MethodWrapper mw = configuration.getMethodWrapper();
        UpdateOrInsertConifg conifg = conifgCache.get(mw.getMethod());
        Connection connection = ConnectionManagement.getConnection(configuration.getGlobalSQLConfiguration().getDataSourceAlias());
        try {
            switch (configuration.getMethodType()){
                case UPDATE:
                    updateMasterProcessor.processorUpdate(sql, sqlValueGroup, configuration, conifg, connection, executePacket);
                    break;
                case INSERT:
                    insertMasterProcessor.processorInsert(sql, sqlValueGroup, configuration, conifg, connection, executePacket);
            }
        }catch (SQLException e){
            Log log = configuration.getLog();
            if (log.isErrorEnabled()) {
                log.error("error on processor update or insert: " + e.getLocalizedMessage());
            }
        }
    }
}
