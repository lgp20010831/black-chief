package com.black.sql_v2.sql_statement;

import com.alibaba.fastjson.JSONObject;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;
import com.black.core.util.StringUtils;
import com.black.sql_v2.AbstractStringSupporter;
import com.black.sql_v2.Environment;
import com.black.sql_v2.JDBCEnvironmentLocal;

import java.sql.Statement;

public class StatementAttributeSetHandler extends AbstractStringSupporter implements JavaSqlStatementHandler {

    public static final String PREFIX = "set statement:";

    public StatementAttributeSetHandler() {
        super(PREFIX);
    }

    @Override
    public Statement handlerJavaStatement(Statement statement, Object param) throws Throwable {
        String txt = getTxt(param);
        JSONObject json = simpleParseJson(txt);
        Environment environment = JDBCEnvironmentLocal.getEnvironment();
        Class<Statement> primordialClass = BeanUtil.getPrimordialClass(statement);
        for (String name : json.keySet()) {
            MethodWrapper setMethod = SetGetUtils.getSetMethod(primordialClass, name);
            if (setMethod != null){
                Object val = json.get(name);
                environment.getLog().trace("[SQL] -- About to inject properties: {} --> {}",
                        val, name);
                setMethod.invoke(statement, val);
            }
        }
        return statement;
    }

    private JSONObject simpleParseJson(String text){
        JSONObject jsonObject = new JSONObject();
        String[] eles = text.split(",");
        for (String ele : eles) {
            if (!StringUtils.hasText(ele)){
                continue;
            }
            String[] kv = ele.split("=");
            if (kv.length != 2)
                throw new IllegalStateException("ill json text: " + ele);
            jsonObject.put(kv[0].trim(), kv[1].trim());
        }
        return jsonObject;

    }
}
