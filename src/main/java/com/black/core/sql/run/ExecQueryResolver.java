package com.black.core.sql.run;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.Param;
import com.black.core.sql.annotation.SelectScript;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.util.Assert;

import java.sql.SQLException;
import java.util.Map;

public class ExecQueryResolver extends SqlRunner implements RunSupport{
    @Override
    public boolean support(MethodWrapper mw) {
        return mw.hasAnnotation(SelectScript.class);
    }

    @Override
    public Object run(MethodWrapper mw, Object[] args, GlobalSQLConfiguration configuration, ClassWrapper<?> cw) throws Throwable {
        ParameterWrapper paramPw = mw.getSingleParameterByAnnotation(Param.class);
        Assert.notNull(paramPw, "not find sql param");
        Assert.trueThrows(!paramPw.getType().equals(String.class), "sql param type must is string");
        String sql = (String) args[paramPw.getIndex()];
        Assert.notNull(sql, "sql is null");
        ParameterWrapper pw = mw.getSingleParameterByType(Map.class);
        Assert.notNull(pw, "map condition is null");
        sql = GlobalMapping.parseAndObtain(sql, true);
        sql = RunSqlParser.parseSql(sql, (Map<String, Object>) args[pw.getIndex()]);
        Object result;
        for (GlobalSQLRunningListener listener : configuration.getApplicationContext().getSQLRunningListeners()) {
            sql = listener.postRunScriptSelectSql(configuration, sql);
        }
        try {
            result = runSql(sql, true,
                    configuration.getConnection(),
                    configuration.getLog(),
                    mw, configuration.getConvertHandler(),
                    configuration);
        }catch (SQLException e){
            throw new SQLSException("parse result error", e);
        }
        return result;
    }
}
