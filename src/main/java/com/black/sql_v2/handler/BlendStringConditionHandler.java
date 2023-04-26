package com.black.sql_v2.handler;

import com.black.core.log.IoLog;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.parse.BlendObject;
import com.black.core.sql.code.parse.CharParser;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.StringUtils;
import com.black.sql.InsertStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.*;
import com.black.sql_v2.utils.SqlV2Utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlendStringConditionHandler extends AbstractStringSupporter implements SqlStatementHandler {

    public static final String PREFIX = "$B:";

    private final Map<String, List<BlendObject>> cache = new ConcurrentHashMap<>();

    public BlendStringConditionHandler() {
        super(PREFIX);
    }

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return !(statement instanceof InsertStatement);
    }

    @Override
    public SqlOutStatement handleStatement(SqlOutStatement statement, Object param) {
        String txt = getTxt(param);
        Environment instance = JDBCEnvironmentLocal.getEnvironment();
        IoLog log = instance.getLog();
        AliasColumnConvertHandler convertHandler = instance.getConvertHandler();
        Map<String, Object> env = JDBCEnvironmentLocal.getEnv();
        Map<String, Object> copyMap = SQLUtils.copyMap(env);
        List<BlendObject> objects = cache.computeIfAbsent(txt, CharParser::parseBlend);
        for (BlendObject blendObject : objects) {
            String operator = blendObject.getName();
            List<String> attributes = blendObject.getAttributes();
            for (String attribute : attributes) {
                boolean and = !attribute.startsWith("!");
                if (!and){
                    attribute = StringUtils.removeIfStartWith(attribute, "!");
                }
                String column = convertHandler.convertColumn(attribute);
                if (copyMap.containsKey(attribute)){
                    Object val = copyMap.remove(attribute);
                    if (SqlV2Utils.isLegalColumn(statement.getTableName(), column)){
                        statement.removeOperation(column, OperationType.SELECT);
                        SqlV2Utils.processor(operator, column, val, and, statement);
                        log.debug("[SQL] -- blend handler replace operation: [{}] to [{}]", column, operator);
                    }
                }
            }
        }
        copyMap.forEach((alias, val) -> {
            String column = convertHandler.convertColumn(alias);
            if (SqlV2Utils.isLegalColumn(statement.getTableName(), column)){
                statement.removeOperation(column, OperationType.SELECT);
                MapArgHandler.wiredParamInStatement(statement, column, val);
                log.debug("[SQL] -- blend handler reset operation: [{}]", column);

            }
        });
        return statement;
    }

}
