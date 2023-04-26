package com.black.core.sql.code.listener;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.SQLListener;
import com.black.core.sql.annotation.SaveTransactionPoint;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.SaveManager;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;


@SQLListener
//目前并不明确, 事务保存点该在什么时候进行回滚
public class SaveTransactionPointListener implements GlobalSQLRunningListener {

    @Override
    public void beforeInvoke(GlobalSQLConfiguration globalSQLConfiguration, MethodWrapper mw, Object[] args) {
        if (mw.hasAnnotation(SaveTransactionPoint.class)) {
            SaveTransactionPoint annotation = mw.getAnnotation(SaveTransactionPoint.class);
            if (!annotation.after()){
                doSave(globalSQLConfiguration, annotation.value());
            }
        }
    }

    @Override
    public Object afterInvoke(GlobalSQLConfiguration globalSQLConfiguration, MethodWrapper mw, Object result) {
        if (mw.hasAnnotation(SaveTransactionPoint.class)) {
            SaveTransactionPoint annotation = mw.getAnnotation(SaveTransactionPoint.class);
            if (annotation.after()){
                doSave(globalSQLConfiguration, annotation.value());
            }
        }
        return result;
    }

    private void doSave(GlobalSQLConfiguration globalSQLConfiguration, String alias){

        Connection connection = ConnectionManagement.getConnection(globalSQLConfiguration.getDataSourceAlias());
        try {
            Savepoint savepoint = connection.setSavepoint();
            SaveManager.registerSavePoint(globalSQLConfiguration.getDataSourceAlias(), alias, savepoint);
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }
}
