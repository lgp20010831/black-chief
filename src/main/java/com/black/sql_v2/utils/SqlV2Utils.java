package com.black.sql_v2.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.black.core.sql.annotation.TableName;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.sup.SqlSequencesFactory;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.json.JsonParser;
import com.black.sql.InsertStatement;
import com.black.sql.SqlOutStatement;
import com.black.sql.UpdateStatement;
import com.black.sql_v2.Environment;
import com.black.sql_v2.GlobalEnvironment;
import com.black.sql_v2.JDBCEnvironmentLocal;
import com.black.sql_v2.TableNameOpt;
import com.black.table.ForeignKey;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.utils.NameUtil;
import com.black.utils.ServiceUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.sql.Connection;
import java.util.*;

public class SqlV2Utils {

    public static String getName(Object bean){
        return BeanUtil.getPrimordialClass(bean).getSimpleName();
    }


    public static String findTableName(Object bean){
        return findTableName(bean, null);
    }

    public static String findTableName(Object bean, AliasColumnConvertHandler handler){
        if (bean == null){
            return null;
        }

        if (bean instanceof TableNameOpt){
            return ((TableNameOpt) bean).getTableName();
        }

        if (bean instanceof Map){
            Object tableName = ((Map<?, ?>) bean).get("tableName");
            return tableName == null ? null : tableName.toString();
        }

        if (bean instanceof Collection){
            return null;
        }

        Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
        TableName annotation = primordialClass.getAnnotation(TableName.class);
        if (annotation != null){
            return annotation.value();
        }

        if (MybatisEnv.isMybatisEnv()){
            return MybatisTableNameHandler.getIbatisTableName(primordialClass);
        }

        String name = NameUtil.getName(bean);
        if (handler != null){
            name = handler.convertColumn(name);
        }
        return name;
    }

    public static JSONObject prepareJson(String jsonText, Object bean){
        GlobalEnvironment environment = GlobalEnvironment.getInstance();
        return prepareJson(environment.getJsonParser(), jsonText, bean);
    }

    //{name: ?}
    //{name: $$alias}
    public static JSONObject prepareJson(@NonNull JsonParser parser, @NonNull String jsonText, Object bean){
        JSONObject json = parser.parseJson(jsonText);
        for (String key : json.keySet()) {
            Object value = json.get(key);
            if (value instanceof String){
                if (value.equals("?")){
                    Object val = ServiceUtils.findVal(bean, key);
                    json.replace(key, val);
                }else if (((String) value).startsWith("$$")){
                    String path = StringUtils.removeIfStartWith(value.toString(), "$$");
                    Object val = ServiceUtils.findVal(bean, path);
                    json.replace(key, val);
                }

            }
        }
        return json;
    }

    public static void putMapToStatement(Map<String, Object> conditionMap, SqlOutStatement statement){
        Environment instance = JDBCEnvironmentLocal.getEnvironment();
        AliasColumnConvertHandler convertHandler = instance.getConvertHandler();
        for (String alias : conditionMap.keySet()) {
            Object value = conditionMap.get(alias);
            String column = convertHandler.convertColumn(alias);
            if (SqlV2Utils.isLegalColumn(statement.getTableName(), column)){
                MapArgHandler.wiredParamInStatement(statement, column, value);
            }
        }
    }

    public static ForeignKey getForeignKeyBySubMetadata(TableMetadata masterTable, TableMetadata subTable){
        PrimaryKey primaryKey = masterTable.firstPrimaryKey();
        return subTable.getForeignByPrimaryNameAndTableName(primaryKey.getName(), masterTable.getTableName());
    }

    public static void setSeqInStatement(SqlOutStatement statement, OperationType type, String... sqlSequences){
        Map<String, Object> env = JDBCEnvironmentLocal.getEnv();
        TableMetadata tableMetadata = tryGetMetadata(statement.getTableName());
        for (String sqlSequence : sqlSequences) {
            sqlSequence = StringUtils.removeFrontSpace(sqlSequence);
            sqlSequence = GlobalMapping.parseAndObtain(sqlSequence, true);

            SqlSequencesFactory.parseSeq(statement, sqlSequence, type, env, tableMetadata);
        }
    }

    @Getter @Setter
    public static class PageInfoWrapper{
        int pageSize, pageNum;
    }

    public static PageInfoWrapper getPageInfoByMap(Map<String, Object> env, String pageSizeName, String pageNumName){
        if (env == null){
            return null;
        }
        Object ps = env.get(pageSizeName);
        Object pn = env.get(pageNumName);
        if (ps == null || pn == null){
            return null;
        }
        PageInfoWrapper wrapper = new PageInfoWrapper();
        wrapper.setPageSize(TypeUtils.castToInt(ps));
        wrapper.setPageNum(TypeUtils.castToInt(pn));
        return wrapper;
    }

    public static boolean isSelectStatement(SqlOutStatement statement){
        return !(statement instanceof InsertStatement || statement instanceof UpdateStatement || statement.isDelete());
    }
    public static String wrapperSelectSqlOfPage(String sql, Integer pageSize, Integer pageNum){
        return StringUtils.linkStr(sql, " limit ", String.valueOf(pageSize),
                " offset ", String.valueOf((pageNum - 1) * pageSize ));
    }

    public static String wrapperSelectCountSql(String sql){
        return StringUtils.linkStr("select count(0) from ( ", sql, " ) s");
    }
    public static boolean isSelectSql(String sql){
         sql = StringUtils.removeFrontSpace(sql);
         return StringUtils.startsWithIgnoreCase(sql, "select");
    }

    public static Object[] addParams(Object[] params, Object... datas){
        ArrayList<Object> list = new ArrayList<>(Arrays.asList(datas));
        list.addAll(Arrays.asList(params));
        return list.toArray();
    }

    public static boolean isLegalColumn(String tableName, String columnName){
        Connection connection = JDBCEnvironmentLocal.getConnection();
        if (connection == null){
            return false;
        }

        TableMetadata tableMetadata = TableUtils.getTableMetadata(tableName, connection);
        Set<String> columnNameSet = tableMetadata.getColumnNameSet();
        return columnNameSet.contains(columnName);
    }

    public static TableMetadata tryGetMetadata(String name){
        Connection connection = JDBCEnvironmentLocal.getConnection();
        return TableUtils.getTableMetadata(name, connection);
    }

    public static void setMapToStatement(SqlOutStatement statement,
                                         AliasColumnConvertHandler handler,
                                         Map<String, Object> mapVal,
                                         TableMetadata tableMetadata){
        Set<String> columnNameSet = tableMetadata.getColumnNameSet();
        PrimaryKey primaryKey = tableMetadata.firstPrimaryKey();
        for (String alias : mapVal.keySet()) {
            String column = handler.convertColumn(alias);
            Object value = mapVal.get(alias);
            //过滤主键
            if (columnNameSet.contains(column) && (primaryKey == null || !primaryKey.getName().equals(column))){
                statement.writeSet(column, MapArgHandler.getString(value), false);
            }
        }
    }

    public static void processor(String operator, String columnName, Object val, boolean and, SqlOutStatement statement){
        if (val == null){
            statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " is null", and);
            return;
        }
        String string = SQLUtils.getString(val);
        switch (operator){
            case "eq":
                if (and){
                    statement.and();
                }else {
                    statement.or();
                }
                statement.writeEq(columnName, string, false);
                break;
            case "like":
            case "LIKE":
                if (and){
                    statement.and();
                }else {
                    statement.or();
                }
                statement.writeLike(columnName, string);
                break;
            case ">":
                statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " > " + string, and);
                break;
            case "<":
                statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " < " + string, and);
                break;
            case "<>":
                statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " <> " + string, and);
                break;
            case "<=":
                statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " <= " + string, and);
                break;
            case ">=":
                statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " >= " + string, and);
                break;
            case "in":
            case "IN":
                List<Object> list = SQLUtils.wrapList(val);
                if (list.isEmpty()){
                    statement.writeAftSeq(SqlOutStatement.getColumnOfStatement(statement, columnName) + " is null", and);
                }else {
                    String[] array = SQLUtils.createW(list.size());
                    statement.writeIn(columnName, false, array);
                }
                break;
            default:
                throw new IllegalStateException("无法支持的操作符: " + operator);
        }
    }

    public static void parseExpression(String expression, SqlOutStatement outStatement, Map<String, Object> env){
        expression = StringUtils.removeFrontSpace(expression);
        expression = GlobalMapping.dynamicParse(expression, env);
        expression = MapArgHandler.parseSql(expression, env);
        if (StringUtils.hasText(expression)){
            String[] fragments = expression.split("\\|");
            for (int i = 0; i < fragments.length; i++) {
                String fragment = fragments[i];
                switch (i){
                    case 0:
                        outStatement.writeReturnColumns(fragment);
                        break;
                    case 1:
                        outStatement.writePre(fragment);
                        break;
                    case 2:

                        boolean join = true;
                        fragment = StringUtils.removeFrontSpace(fragment);
                        if (fragment.startsWith("and") || fragment.startsWith("or")){
                            join = false;
                        }
                        if (join){
                            outStatement.writeAft("and");
                        }
                        outStatement.writeAft(fragment);
                        break;
                    case 3:
                        outStatement.writeLastSql(fragment);
                        break;
                }
            }
        }
    }
}
