package com.black.sql_v2.listener;

import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.unc.OperationType;
import com.black.sql.InsertStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql.UpdateStatement;
import com.black.sql_v2.*;
import com.black.sql_v2.utils.SqlV2Utils;

public class SeqMetadataListener implements SqlListener{

    @Override
    public void beforeFlush(SqlOutStatement statement, SqlExecutor executor) {
        run(statement);
        SqlListener.super.beforeFlush(statement, executor);
    }

    private void run(SqlOutStatement statement){
        Environment environment = JDBCEnvironmentLocal.getEnvironment();
        boolean where = true;
        if (statement instanceof InsertStatement){
            where = false;
            SqlSeqPack pack = environment.getPack(SqlType.INSERT);
            SqlV2Utils.setSeqInStatement(statement, OperationType.INSERT, pack.getSeqQueue().toArray(new String[0]));
            pack.getKeyValueMap().forEach((c, v) -> {
                statement.insertValue(c, MapArgHandler.getString(v), false);
            });
        }else if (statement instanceof UpdateStatement){
            SqlSeqPack setPack = environment.getPack(SqlType.SET);
            SqlV2Utils.setSeqInStatement(statement, OperationType.UPDATE, setPack.getSeqQueue().toArray(new String[0]));
            setPack.getKeyValueMap().forEach((c, v) -> {
                statement.writeSet(c, MapArgHandler.getString(v), false);
            });
        }

        if (where){
            SqlSeqPack pack = environment.getPack(SqlType.WHERE);
            SqlV2Utils.setSeqInStatement(statement, OperationType.SELECT, pack.getSeqQueue().toArray(new String[0]));
            pack.getKeyValueMap().forEach((c, v) -> {
                statement.writeEq(c, MapArgHandler.getString(v), false);
            });
        }
    }


}
