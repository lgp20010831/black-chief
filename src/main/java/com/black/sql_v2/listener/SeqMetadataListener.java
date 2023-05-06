package com.black.sql_v2.listener;

import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.sql.InsertStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql.UpdateStatement;
import com.black.sql_v2.*;
import com.black.sql_v2.utils.SqlV2Utils;
import com.black.sql_v2.with.GeneratePrimaryManagement;
import com.black.sql_v2.with.WaitGenerateWrapper;
import com.black.utils.ServiceUtils;

import java.util.List;

public class SeqMetadataListener implements SqlListener{

    @Override
    public void beforeFlush(SqlOutStatement statement, SqlExecutor executor) {
        run(statement);
        SqlListener.super.beforeFlush(statement, executor);
    }

    private void run(SqlOutStatement statement){
        Environment environment = JDBCEnvironmentLocal.getEnvironment();
        AliasColumnConvertHandler aliasColumnConvertHandler = environment.getConvertHandler();
        boolean where = true;
        if (statement instanceof InsertStatement){
            where = false;
            SqlSeqPack pack = environment.getPack(SqlType.INSERT);
            Object attachment = JDBCEnvironmentLocal.attachment();
            List<Object> list = attachment == null ? null : SQLUtils.wrapList(attachment);
            SqlV2Utils.setSeqInStatement(statement, OperationType.INSERT, pack.getSeqQueue().toArray(new String[0]));
            pack.getKeyValueMap().forEach((c, v) -> {
                String value = MapArgHandler.getString(v);
                statement.removeInsertValue(c);
                statement.insertValue(c, value, false);
                if (list != null){
                    String alias = aliasColumnConvertHandler.convertAlias(c);
                    for (Object ele : list) {
                        ServiceUtils.setProperty(ele, alias, value);
                    }
                }
            });
        }else if (statement instanceof UpdateStatement){
            WaitGenerateWrapper generateWrapper = GeneratePrimaryManagement.get();
            boolean allow = generateWrapper != null && generateWrapper.getPassId().equals(JDBCEnvironmentLocal.getPack().getPassID());
            SqlSeqPack setPack = environment.getPack(SqlType.SET);
            SqlV2Utils.setSeqInStatement(statement, OperationType.UPDATE, setPack.getSeqQueue().toArray(new String[0]));
            setPack.getKeyValueMap().forEach((c, v) -> {
                String value = MapArgHandler.getString(v);
                statement.removeUpdateValue(c);
                statement.writeSet(c, value, false);
                if (allow){
                    AliasColumnConvertHandler convertHandler = generateWrapper.getConvertHandler();
                    Object bean = generateWrapper.getBean();
                    String alias = convertHandler.convertAlias(c);
                    ServiceUtils.setProperty(bean, alias, value);
                }
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
