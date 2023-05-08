package com.black.result_set;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.xml.PrepareSource;
import com.black.scan.ScannerManager;
import com.black.utils.ServiceUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 李桂鹏
 * @create 2023-05-08 10:30
 */
@SuppressWarnings("all")
public class ResultSetHandlerManager {

    private final static LinkedBlockingQueue<ResultSetHandler> HANDLERS = new LinkedBlockingQueue<>();

    static {
        HANDLERS.addAll(ServiceUtils.scanAndLoad("com.black.result_set.impl", ResultSetHandler.class));
    }

    public static Object resolve(SQLMethodType methodType, MethodWrapper mw, ResultSet resultSet, PrepareSource prepareSource){
        Class<?> returnType = mw.getReturnType();
        for (ResultSetHandler handler : HANDLERS) {
            if (handler.support(methodType, mw)) {
                try {
                    return handler.resolve(resultSet, returnType, mw, prepareSource, methodType);
                } catch (SQLException e) {
                    throw new SQLSException(e);
                }
            }
        }
        return null;
    }
}
