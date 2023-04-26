package com.black.sql_v2.handler;

import com.alibaba.fastjson.JSONObject;
import com.black.sql.InsertStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.AbstractStringSupporter;
import com.black.sql_v2.Environment;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.utils.JsonDrawer;
import com.black.sql_v2.utils.SqlV2Utils;

import java.util.Map;

public class JSONArgStatementHandler extends AbstractStringSupporter implements SqlStatementHandler {

    public static final String PREFIX = "JSON:";

    public JSONArgStatementHandler() {
        super(PREFIX);
    }

    @Override
    public boolean support(Object param) {
        if (param instanceof JsonDrawer){
            return true;
        }
        return super.support(param);
    }

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return!(statement instanceof InsertStatement);
    }

    @Override
    public SqlOutStatement handleStatement(SqlOutStatement statement, Object param) {
        JSONObject jsonObject;
        if (param instanceof JsonDrawer){
            jsonObject = ((JsonDrawer) param).reach();
        }else {
            String txt = getTxt(param);
            Environment environment = JDBCEnvironmentLocal.getEnvironment();
            jsonObject = environment.getJsonParser().parseJson(txt);
        }
        Map<String, Object> env = JDBCEnvironmentLocal.getEnv();
        if (env == null){
            JDBCEnvironmentLocal.setEnv(jsonObject);
        }else {
            env.putAll(jsonObject);
        }
        SqlV2Utils.putMapToStatement(jsonObject, statement);
        return statement;
    }
}
