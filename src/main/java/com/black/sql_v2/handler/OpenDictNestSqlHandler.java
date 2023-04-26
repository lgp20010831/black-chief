package com.black.sql_v2.handler;

import com.black.nest.Dict;
import com.black.nest.NestManager;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.AbstractStringSupporter;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.utils.DictUtils;
import com.black.sql_v2.utils.SqlV2Utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenDictNestSqlHandler extends AbstractStringSupporter implements SqlStatementHandler{

    public static final String PREFIX = "open dict";

    public OpenDictNestSqlHandler() {
        super(PREFIX);
    }

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return SqlV2Utils.isSelectStatement(statement);
    }

    @Override
    public SqlOutStatement handleStatement(SqlOutStatement statement, Object param) {
        String[] expressions = getExpressions(statement.getTableName());
        JDBCEnvironmentLocal.getEnvironment().getLog().trace("OPEN DICT expressions: {}", Arrays.toString(expressions));
        return DictUtils.handlerTxt(expressions, statement);
    }

    protected String[] getExpressions(String tableName){
        List<Dict> dicts = getDicts(tableName);
        List<String> expressionList = new ArrayList<>();
        int size = 0;
        String prefix = "d";
        for (Dict dict : dicts) {
            String alias = prefix + ++size;
            StringBuilder builder = new StringBuilder();
            builder.append(alias).append(".").append(dict.getResultName())
                    .append(" ").append(dict.getResultNameAlias())
                    .append("|")
                    .append("left join ")
                    .append(dict.getDictTableName())
                    .append(" ")
                    .append(alias)
                    .append(" ")
                    .append("on")
                    .append(" ");
            if (dict.getPCodeName() != null){
                builder.append(alias).append(".").append(dict.getPCodeName())
                        .append("= '").append(dict.getPCodeValue())
                        .append("'")
                        .append(" and ");
            }
                    builder.append(alias).append(".").append(dict.getCodeName()).append(" = ")
                            .append("r.")
                            .append(dict.getSourceFieldName())
                            .append("|");
            expressionList.add(builder.toString());
        }

        return expressionList.toArray(new String[0]);
    }

    protected List<Dict> getDicts(String tableName){
        Connection connection = JDBCEnvironmentLocal.getConnection();
        return NestManager.queryDictBySource(tableName, connection);
    }
}
