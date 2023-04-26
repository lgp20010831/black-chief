package com.black.core.sql.code;

import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.session.PipelineSession;
import com.black.core.sql.code.session.SQLSignalSession;
import com.black.core.sql.code.session.TransactionSQLSession;
import lombok.NonNull;

public class TransactionSQLApplicationContext extends BaseSQLApplicationContext{

    public TransactionSQLApplicationContext(@NonNull GlobalSQLConfiguration configuration) {
        super(configuration);
        registerGlobalSQLRunningListener(new TransactionSQLManagement.TransactionConnectionListener(configuration));
        ConnectionManagement.registerApplicationContext(this);
    }

    @Override
    public SQLSignalSession openSession() {
        TransactionSQLSession sqlSession = new PipelineSession(configuration);
        sessionManager.add(sqlSession);
        return sqlSession;
    }
}
