package com.black.core.sql.code.listener;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.UpdateOrInsertConifg;
import com.black.core.sql.code.ill.StopSqlInvokeException;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.SQLSignalSession;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.SqlValue;
import com.black.core.sql.unc.SqlVariable;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public abstract class AbstractProcessor {

    protected String createSelectSql(String sql, Configuration configuration, UpdateOrInsertConifg conifg){
        if (!sql.contains("where")) {
            throw new StopSqlInvokeException("update sql no condition, is too DANGER");
        }
        String condition = StringUtils.split(sql, "where", 2, "unresolved UPDATE statement: " + sql)[1];
        SqlOutStatement statement = SqlWriter.selectCount(configuration.getTableName());
        statement.writeAft(condition);
        synchronized (conifg.getExistCondition()){
            for (String ec : conifg.getExistCondition()) {
                String obtain = GlobalMapping.parseAndObtain(ec);
                statement.writeAft("and");
                statement.writeAft(obtain);
            }
        }
        return statement.flush().toString();
    }

    protected void reduce(List<SqlValue> sqlValueList, int len){
        if (len > 0){
            for (SqlValue sqlValue : sqlValueList) {
                SqlVariable variable = sqlValue.getVariable();
                variable.setIndex(variable.getIndex() - len);
            }
        }

    }

    protected void recovery(List<SqlValue> sqlValueList, int len){
        if (len > 0){
            for (SqlValue sqlValue : sqlValueList) {
                SqlVariable variable = sqlValue.getVariable();
                variable.setIndex(variable.getIndex() + len);
            }
        }
    }

    protected boolean exist(String selectSql, Configuration configuration, List<SqlValue> selectSqlValues) throws SQLException {
        SQLSignalSession session = configuration.getSession();
        ExecuteBody executeBody = session.pipelineSelect(selectSql, Collections.singletonList(new SqlValueGroup(selectSqlValues)));
        ResultSet queryResult = executeBody.getQueryResult();
        int count = 0;
        try {
            while (queryResult.next()) {
                count = queryResult.getInt(1);
            }
        }finally {
            SQLUtils.closeResultSet(queryResult);
        }
        return count > 0;
    }
}
