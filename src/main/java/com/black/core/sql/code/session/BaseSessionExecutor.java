package com.black.core.sql.code.session;

import com.black.core.sql.code.StatementWrapper;

import java.sql.SQLException;


public class BaseSessionExecutor extends AbstractSessionExecutor{


    public BaseSessionExecutor(SQLSignalSession session) {
        super(session);
    }

    @Override
    void addBatch(StatementWrapper sw, int batchSize, int batchCount) throws SQLException {
        sw.addBatch();
    }

}
