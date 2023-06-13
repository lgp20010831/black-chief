package com.black.core.sql.code.util;

import com.alibaba.fastjson.JSONObject;

import com.black.core.json.JsonUtils;
import com.black.core.json.ReflexUtils;
import com.black.core.query.ArrayUtils;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.*;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.StatementValueSetDisplayConfiguration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.util.StringUtils;
import com.black.core.util.Utils;
import com.black.table.SQLTablesException;
import com.black.table.TableMetadata;
import com.black.table.TableQuasiEntity;
import lombok.NonNull;
import org.apache.ibatis.jdbc.ScriptRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

import static com.black.core.sql.code.SqlHelper.*;
import static com.black.table.TableUtils.getTableMetadata;

public class SQLUtils {

    public static final String KG = " ";

    public static <T> List<T> wrapperList(T ele){
        return Collections.singletonList(ele);
    }

    public static void setStatementValue(PreparedStatement statement,
                                         int index,
                                         Object value,
                                         int type) throws SQLException {
        setStatementValue(statement, index, value, type, null);
    }

    public static void setStatementValue(PreparedStatement statement,
                                         int index,
                                         Object value,
                                         int type,
                                         StatementValueSetDisplayConfiguration configuration) throws SQLException {
        if (value == null){
            statement.setNull(index, type);
        }else {
            if (configuration != null){
                if(configuration.isSetObjectOfType()){
                    statement.setObject(index, value, type);
                }else {
                    statement.setObject(index, value);
                }
            }else{
                statement.setObject(index, value, type);
            }
        }
    }

    public static Connection getConnection(DataSource dataSource){
        try {
            return dataSource != null ? dataSource.getConnection() : null;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    public static <K, V> Map<K, V> copyMap(Map<K, V> map){
        if (map == null){
            return new HashMap<>();
        }
        return new LinkedHashMap<>(map);
    }


    public static <E> E getSingle(List<E> list){
        if (Utils.isEmpty(list)){
            return null;
        }else if (list.size() == 1){
            return list.get(0);
        }else {
            throw new SQLSException("The return value type is specified as a single instance, " +
                    "but there are multiple results");
        }
    }

    public static Map<String, Object> wrapMap(Object ele){
        if (ele == null){
            return new HashMap<>();
        }

        if (ele instanceof Map){
            return (Map<String, Object>) ele;
        }

        return JsonUtils.letJson(ele);
    }

    public static List<Object> wrapList(Object ele){
        if (ele == null){
            return new ArrayList<>();
        }
        if (ele instanceof Collection){
            if (ele instanceof List){
                return (List<Object>) ele;
            }
            return new ArrayList<>((Collection<?>) ele);
        }
        ArrayList<Object> list = new ArrayList<>();
        if (ele.getClass().isArray()) {
            list.addAll(Arrays.asList((Object[]) ele));
        }else {
            list.add(ele);
        }
        return list;
    }

    public static void closeStatement(Statement statement){
        if (statement != null){
            try {
                if (!statement.isClosed()) {
                    statement.close();
                }
            } catch (SQLException e) {
                //ignore
            }
        }
    }

    public static void closeConnection(Connection connection){
        if (connection != null){
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                //ignore
            }
        }
    }

    public static void closeResultSet(ResultSet resultSet){
        if (resultSet != null){
            try {
                if (!resultSet.isClosed()) {
                    resultSet.close();
                }
            }catch (SQLException e){
                //ignore
            }
        }
    }

    public static Object loopFind(Map<String, Object> originalArgs, String name){
        if (originalArgs.containsKey(name)) {
            return originalArgs.get(name);
        }
        for (Object value : originalArgs.values()) {
            if (value instanceof Map){
                Object result = loopFind((Map<String, Object>) value, name);
                if (result != null){
                    return result;
                }
            }else
            if (value instanceof Collection){
                for (Object ele : ((Collection) value)) {
                    if (ele instanceof Map){
                        Object find = loopFind((Map<String, Object>) ele, name);
                        if (find != null){
                            return find;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String getLogString(Configuration configuration, String layerName){
        return StringUtils.linkStr("[", configuration.getClass().getSimpleName(), "] ", layerName);
    }

    public static String[] createW(int size){
        String[] strings = new String[size];
        for (int i = 0; i < size; i++) {
            strings[i] = "?";
        }
        return strings;
    }

    public static void executeSql(String sql, Connection connection){
        Statement statement = null;
        try {
            (statement = connection.createStatement()).execute(sql);
        } catch (SQLException e) {
            closeStatement(statement);
        }
    }

    public static int runSql(String sql, Connection connection){
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLSException(e);
        }finally {
            if (statement != null){
                try {
                    statement.close();
                } catch (SQLException e) {}
            }
        }
    }

    public static Statement callSql(String sql, Connection connection){
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
        return statement;
    }


    public static Object findMapping(String val, ExecutePacket ep){
        String topic = SQLUtils.parseTopic(val);
        Object arg = MapArgHandler.getValue(ep.getOriginalArgs(), topic);
        if (arg == null){
            return "null";
        }else {
            return arg;
        }
    }

    public static String merge(String... cns){
        StringBuilder builder = new StringBuilder();
        ArrayUtils.stateFor(cns, (s, last, first) -> {
            builder.append(s);
            if (!last){
                builder.append(",");
            }
        });
        return builder.toString();
    }


    public static String getPureString(String seq){
        seq = StringUtils.removeIfStartWith(StringUtils.removeFrontSpace(seq), "'");
        seq = StringUtils.removeIfEndWith(StringUtils.removeTrailingSpace(seq), "'");
        return seq;
    }

    public static String getString(Object val){
        if(val == null){
            return "null";
        }
        if (val instanceof Number || val.getClass().equals(boolean.class) || val instanceof Boolean){
            return val.toString();
        }
        return "'" + val.toString() + "'";
    }

    public static String cast(Object notnulArg){
        Class<?> type = notnulArg.getClass();
        if (String.class.equals(type)) {
            return "'" + notnulArg + "'";
        }else return notnulArg.toString();
    }

    public static String parseTopic(String topic){
        if (topic.startsWith("#{")){

            if (!topic.endsWith("}")){
                throw new IllegalStateException("主题 " + topic + " 缺少结束符: }");
            }
            return topic.substring(2, topic.length() - 1);
        }else {
            return topic;
        }
    }

    public static void insertList(TableMetadata tableMetadata,
                                  List<Map<String, Object>> datas,
                                  Connection connection) throws SQLException {
        insertList(tableMetadata, datas, connection, null);
    }

    public static void insertList(String tableName,
                                  List<Map<String, Object>> datas,
                                  Connection connection) throws SQLException {
        insertList(getTableMetadata(tableName, connection), datas, connection, null);
    }

    public static void insertList(String tableName,
                                  List<Map<String, Object>> datas,
                                  Connection connection,
                                  AliasColumnConvertHandler convertHandler) throws SQLException {
        insertList(getTableMetadata(tableName, connection), datas, connection, convertHandler);
    }

    public static void insertList(TableMetadata tableMetadata,
                                  List<Map<String, Object>> datas,
                                  Connection connection,
                                  AliasColumnConvertHandler convertHandler) throws SQLException {
        if (datas.isEmpty()) return;
        List<String> columnNameList = tableMetadata.getColumnNameList();
        String sql = StringUtils.linkStr("insert into ", tableMetadata.getTableName(),
                getColumnNamesSql(columnNameList), "values", getValuesSuffix(getDefaultSqlSeq(columnNameList.size()), 1));
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        try {
            int i = 1;
            for (Map<String, Object> data : datas) {
                for (String columnName : columnNameList) {
                    int type = tableMetadata.getColumnMetadata(columnName).getType();
                    Object val = data.get(convertHandler == null ? columnName : convertHandler.convertAlias(columnName));
                    if (val == null){
                        preparedStatement.setNull(i++, type);
                    }else {
                        preparedStatement.setObject(i++, val, type);
                    }
                }
                preparedStatement.addBatch();
                i = 1;
            }
            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
        }finally {
            preparedStatement.close();
        }
    }

    public static void transfer(String selectSql,
                                String targetName,
                                Connection connection) throws SQLException{
        transfer(selectSql, targetName, connection, null);
    }

    public static void transfer(String selectSql,
                                String targetName,
                                Connection connection,
                                Map<String, String> columnMapping) throws SQLException{
        transfer(selectSql, targetName, connection, columnMapping, null);
    }

    public static void transfer(String selectSql,
                                String targetName,
                                Connection connection,
                                Map<String, String> columnMapping,
                                AliasColumnConvertHandler convertHandler) throws SQLException {
        List<Map<String, Object>> result = runJavaSelect(selectSql, connection, convertHandler);
        if (columnMapping != null){
            for (Map<String, Object> map : result) {
                columnMapping.forEach((o, n) ->{
                    map.put(n, map.remove(o));
                });
            }
        }
        insertList(targetName, result, connection, convertHandler);
    }

    public static List<Map<String, Object>> processorResultSet(@NonNull ResultSet resultSet, @NonNull TableMetadata metadata) throws SQLException {
        if (resultSet.isClosed()) {
            throw new SQLTablesException("result set is already closed");
        }
        List<Map<String, Object>> quasiEntities = new ArrayList<>();
        while (resultSet.next()) {
            TableQuasiEntity quasiEntity = new TableQuasiEntity(metadata);
            for (String columnName : metadata.getColumnNameSet()) {
                Object result = resultSet.getObject(columnName);
                quasiEntity.putResult(columnName, result);
            }
            quasiEntities.add(quasiEntity);
        }
        return quasiEntities;
    }

    public static void runSQL(InputStream in, Connection connection){
        ScriptRunner runner = new ScriptRunner(connection);
        try {
            runner.runScript(new InputStreamReader(in));
        }catch (Throwable e){
            throw new SQLSException(e);
        }
    }

    public static ResultSet runQuery(String sql, Connection connection){
        PreparedStatement prepareStatement;
        try {
            prepareStatement = connection.prepareStatement(sql);
            return prepareStatement.executeQuery();
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    public static <T extends Map<String, Object>> List<T> parseResultSet(ResultSet resultSet, Class<T> mapType){
        List<T> list = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()){
                T map = ReflexUtils.instance(mapType);
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    map.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                list.add(map);
            }
            SQLUtils.closeResultSet(resultSet);
            return list;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    public static List<Map<String, Object>> parseResultSet(ResultSet resultSet){
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()){
                Map<String, Object> map = new HashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    map.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                list.add(map);
            }
            SQLUtils.closeResultSet(resultSet);
            return list;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    public static List<Map<String, Object>> runSelect(String sql, Connection connection, String tableName) throws SQLException {
        TableMetadata tableMetadata = getTableMetadata(tableName, connection);
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ResultSet resultSet = prepareStatement.executeQuery();
        List<Map<String, Object>> result = parseResult(resultSet, tableMetadata);
        SQLUtils.closeStatement(prepareStatement);
        return result;
    }

    public static List<Map<String, Object>> runSelect(String sql, Connection connection, TableMetadata tableMetadata) throws SQLException {
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ResultSet resultSet = prepareStatement.executeQuery();
        List<Map<String, Object>> result = parseResult(resultSet, tableMetadata);
        SQLUtils.closeStatement(prepareStatement);
        return result;
    }


    public static List<Map<String, Object>> runJavaSelect(String sql, Connection connection, AliasColumnConvertHandler handler) throws SQLException {
        PreparedStatement prepareStatement = connection.prepareStatement(sql);
        ResultSet resultSet = prepareStatement.executeQuery();
        List<Map<String, Object>> result = parseJavaResult(resultSet, handler);
        SQLUtils.closeStatement(prepareStatement);
        return result;
    }

    public static <T extends Map<String, Object>> List<T> parseJavaResult(ResultSet resultSet, Class<T> mapType, AliasColumnConvertHandler handler){
        List<T> list = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()){
                T map = ReflexUtils.instance(mapType);
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    map.put(handler == null ? columnName : handler.convertAlias(columnName), resultSet.getObject(i));
                }
                list.add(map);
            }
            SQLUtils.closeResultSet(resultSet);
            return list;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    public static List<JSONObject> parseJavaJsonResult(ResultSet set, AliasColumnConvertHandler handler) throws SQLException {
        if (set.isClosed()) {
            throw new SQLTablesException("result set is already closed");
        }
        List<JSONObject> resultList = new ArrayList<>();
        ResultSetMetaData metaData = set.getMetaData();
        while (set.next()){
            JSONObject resultMap = new JSONObject(true);
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                resultMap.put(handler == null ? columnName : handler.convertAlias(columnName), set.getObject(i));
            }
            resultList.add(resultMap);
        }
        SQLUtils.closeResultSet(set);
        return resultList;
    }

    public static List<Map<String, Object>> parseJavaResult(ResultSet set, AliasColumnConvertHandler handler) throws SQLException {
        if (set.isClosed()) {
            throw new SQLTablesException("result set is already closed");
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = set.getMetaData();
        while (set.next()){
            Map<String, Object> resultMap = new LinkedHashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                resultMap.put(handler == null ? columnName : handler.convertAlias(columnName), set.getObject(i));
            }
            resultList.add(resultMap);
        }
        SQLUtils.closeResultSet(set);
        return resultList;
    }

    public static List<Map<String, Object>> parseResult(ResultSet set, TableMetadata metadata) throws SQLException {
        if (set.isClosed()) {
            throw new SQLTablesException("result set is already closed");
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        while (set.next()){
            Map<String, Object> resultMap = new HashMap<>();
            for (String name : metadata.getColumnNameSet()) {
                try {
                    resultMap.put(name, set.getObject(name));
                }catch (SQLException ex){
                    //ignore
                }
            }
            resultList.add(resultMap);
        }
        SQLUtils.closeResultSet(set);
        return resultList;
    }


    public static String getConditionSqlByMap(Map<String, String> map){
        StringBuilder builder = new StringBuilder();
        for (String column : map.keySet()) {
            builder.append(KG);
            builder.append(column);
            builder.append("=");
            builder.append(map.get(column));
            builder.append(KG);
            builder.append("and");
            builder.append(KG);
        }
        String txt = builder.toString();
        return StringUtils.removeIfEndWith(txt, "and ");
    }


    public static int getIndexFromParamName(String paramName){
        if (!StringUtils.hasText(paramName)){
            throw new SQLSException("paramName is not has text");
        }
        int length = paramName.length();
        String lastChar = paramName.substring(length - 1);
        try {

            return Integer.parseInt(lastChar);
        }catch (NumberFormatException nfe){
            throw new SQLSException("wrapper parameter definition of exception: " + paramName);
        }
    }


    public static Map<String, String> parseSetSQL(String setSql){
        String[] split = setSql.split(",");
        Map<String, String> map = new HashMap<>();
        for (String kv : split) {
            String[] skv = StringUtils.split(kv, "=", 2, "解析 set 语句异常: " + kv);
            map.put(skv[0], skv[1]);
        }
        return map;
    }




    public static String getSqlFileStringStream(String filepath){

        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filepath);){
            byte[] buf;
            in.read(buf = new byte[in.available()]);
            return new String(buf);
        } catch (IOException e) {
            throw new SQLSException("read file error", e);
        }
    }
}
