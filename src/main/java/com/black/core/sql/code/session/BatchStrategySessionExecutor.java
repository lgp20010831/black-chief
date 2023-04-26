package com.black.core.sql.code.session;

import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.StatementWrapper;

import java.sql.SQLException;


public class BatchStrategySessionExecutor extends AbstractSessionExecutor{


    protected BatchStrategySessionExecutor(SQLSignalSession session) {
        super(session);
    }


    public static void main(String[] args) {
        System.out.println(94000%2000);
    }

    @Override
    void addBatch(StatementWrapper sw, int batchSize, int batchCount) throws SQLException {
        sw.addBatch();
        int batchStrategy = session.getConfiguration().getBatchStrategy();
        if (batchSize % batchStrategy == 0){
            Log log = session.getConfiguration().getLog();
            if (log.isDebugEnabled()) {
                log.debug("==> batch submission:[" + batchStrategy + "], how many times:[" + batchSize / batchStrategy + "]");
            }
            sw.executeBatch();
            sw.clearBatch();
        }
    }
}
