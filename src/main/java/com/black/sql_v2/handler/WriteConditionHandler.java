package com.black.sql_v2.handler;

import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.sql.InsertStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.AbstractStringSupporter;
import com.black.sql_v2.JDBCEnvironmentLocal;

import java.util.Map;

/**
 * @author shkstart
 * @create 2023-04-14 10:09
 */
public class WriteConditionHandler extends AbstractStringSupporter implements SqlStatementHandler {

    public static final String PREFIX = "$W: ";

    public WriteConditionHandler() {
        super(PREFIX);
    }

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return !(statement instanceof InsertStatement);
    }

    @Override
    public SqlOutStatement handleStatement(SqlOutStatement statement, Object param) {
        String txt = getTxt(param);
        txt = GlobalMapping.parseAndObtain(txt);
        Map<String, Object> env = JDBCEnvironmentLocal.getEnv();
        if (env != null){
            txt = MapArgHandler.parseSql(txt, env);
        }
        statement.writeCondition(txt);
        return statement;
    }
}
