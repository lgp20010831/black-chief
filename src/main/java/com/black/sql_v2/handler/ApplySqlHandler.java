package com.black.sql_v2.handler;

import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.util.StringUtils;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.AbstractStringSupporter;
import com.black.sql_v2.JDBCEnvironmentLocal;

import java.util.Map;

public class ApplySqlHandler extends AbstractStringSupporter implements SqlStatementHandler{

    public static final String PREFIX = "$A:";

    public ApplySqlHandler() {
        super(PREFIX);
    }

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return true;
    }

    @Override
    public SqlOutStatement handleStatement(SqlOutStatement statement, Object param) {
        String applySql = getTxt(param);
        if (StringUtils.hasText(applySql)){
            Map<String, Object> env = JDBCEnvironmentLocal.getEnv();
            applySql = GlobalMapping.parseAndObtain(applySql, true);
            applySql = MapArgHandler.parseSql(applySql, env);
            statement.writeLastSql(applySql);
        }
        return statement;
    }

}
