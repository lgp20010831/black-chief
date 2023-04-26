package com.black.sql;

import com.black.core.annotation.Important;
import com.black.core.query.ArrayUtils;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.unc.OperationType;
import com.black.core.sql.unc.SqlVariable;
import com.black.core.sql.unc.ValueAndType;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.core.util.Utils;
import com.black.table.TableMetadata;

import java.util.*;
import java.util.function.Consumer;

import static com.black.sql.JdbcSqlUtils.getString;

@Important
@SuppressWarnings("all")
public class SqlOutStatement extends AbstractStatement{

    protected String tableName;

    protected SQLMethodType methodType;

    protected StringBuilder pre;

    protected boolean delete = false;

    /**
     * 该string buildr 主要缓存条件 sql 语句
     */
    protected StringBuilder aft;

    //缓存last sql
    protected StringBuilder lsb;

    /**
     * 保留操作属性, 默认为 false
     * 当该值为 true 时, 所有的操作都会被等装成
     * {@link Operation} 对象, 并缓存, 不会立即渲染到最终
     * 的sql上, 所有中途可以对之前的操作进行替换和删除
     * 如 在某一刻 id 为不确定值 则 id = ?
     * 但是之后需要将 id 进行修改, 则需要开启此功能
     */

    private boolean andCon = true;

    /**
     * 每次添加条件之间连接符是否用 and, 如果为 false 则用 or
     * 替换方法调用 filp 方法
     */

    protected int whereIndex = 0;

    protected boolean autoEscape = false;

    private boolean asTable = false;

    public static String alias = "r";

    protected final List<String> lastSql = new ArrayList<>();

    public SqlOutStatement(){
        pre = new StringBuilder();
        aft = new StringBuilder();
        lsb = new StringBuilder();
    }

    public SqlOutStatement(String preStr, String aftStr){
        pre = new StringBuilder(preStr);
        aft = new StringBuilder(aftStr);
        lsb = new StringBuilder();
    }

    public String getTableName() {
        return tableName;
    }

    public void setAutoEscape(boolean autoEscape) {
        this.autoEscape = autoEscape;
    }

    public boolean isAutoEscape() {
        return autoEscape;
    }

    public void setAsTable(boolean asTable) {
        this.asTable = asTable;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isDelete() {
        return delete;
    }

    public int getWhereIndex() {
        return ++whereIndex;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public static String getColumnOfStatement(SqlOutStatement sqlOutStatement, String column){
        if (sqlOutStatement == null || !sqlOutStatement.asTable){
            return column;
        }
        return sqlOutStatement.alias + "." + column;
    }

    public static void main(String[] args) {
        SqlOutStatement statement = new SqlOutStatement();
        statement.setAsTable(false);
        System.out.println(getColumnOfStatement(statement, "name"));
    }

    public String getColumnName(String columnName){
        return asTable ? alias + "." + columnName : columnName;
    }

    public boolean isAsTable() {
        return asTable;
    }

    public SqlOutStatement resetTableName(String newTableName){
        tableName = newTableName;
        return this;
    }

    public SqlOutStatement close(){
        andCon = true;
        aft = new StringBuilder();
        whereIndex = 0;
        operations.clear();
        indexVariableCache.clear();
        indexOperationCache.clear();
        variables.clear();
        return this;
    }

    public SqlOutStatement writeReturnColumns(String sql){
        sql = StringUtils.removeIfStartWith(StringUtils.removeFrontSpace(sql), ",");
        String string = pre.toString();
        int from = string.indexOf("from");
        if (from != -1){
            string = string.substring(0, from) + ", " + sql + " " + string.substring(from);
            pre.delete(0, pre.length());
            pre.append(string);
        }
        return this;
    }


    public SqlOutStatement flush(){
        try {
            flushing = true;
            for (Operation operation : operations) {
                invokeOpeation(operation);
            }
            for (String sql : lastSql) {
                doWriteLsb(sql);
            }
            return this;
        }finally {
            flushing = false;
            indexOperationCache.clear();
            operations.clear();
            lastSql.clear();
        }
    }

    public SqlOutStatement doWriteLsb(String sql){
        if (StringUtils.hasText(sql)){
            sql = StringUtils.addIfNotStartWith(sql, " ");
            lsb.append(sql);
        }
        return this;
    }

    public SqlOutStatement calibration(TableMetadata tableMetadata){
        return calibration(tableMetadata, getOperations());
    }

    public SqlOutStatement calibration(TableMetadata tableMetadata, List<Operation> operations){
        Set<String> columnNameSet = tableMetadata.getColumnNameSet();
        operations.removeIf(o -> {
            SqlOutStatement othereStatement = o.getOthereStatement();
            if(othereStatement != null){
                calibration(tableMetadata, othereStatement.getOperations());
                return othereStatement.getOperations().size() == 0;
            }else {
                return !columnNameSet.contains(o.getColumnName()) && !NO_COLUMN_OPERATION.equals(o.getColumnName());
            }
        });
        return this;
    }

    public SqlOutStatement writeCondition(String sql){
        registerOperations(operation -> {
            String value = operation.getValue();
            boolean hasConn = false;
            value = StringUtils.removeFrontSpace(value);
            if (StringUtils.startsWithIgnoreCase(value, "and") ||
                    StringUtils.startsWithIgnoreCase(value, "or")){
                hasConn = true;
            }
            if (!hasConn){
                start();
            }

            value = StringUtils.removeTrailingSpace(value);
            writeAft(value);
        }, null, sql, false, OperationType.SELECT);
        return this;
    }

    @Override
    public String toString() {
        String toAftString = checkAft(StringUtils.removeFrontSpace(toAftString()));
        String preString = checkPre(StringUtils.removeFrontSpace(toPreString()));
        String lsbString = lsb.toString();
        Map<String, String> argMap = new HashMap<>();
        Assert.notNull(tableName, "table name is null");
        argMap.put("tableName", tableName);
        preString = Utils.parse(preString, "#{", "}", argMap);
        return StringUtils.linkStr(preString,
                StringUtils.hasText(toAftString) ? " where " + toAftString : "",
                lsbString);
    }

    public String getCheckAft(){
        return checkAft(StringUtils.removeFrontSpace(toAftString()));
    }

    public String getCheckPre(){
        return checkPre(StringUtils.removeFrontSpace(toPreString()));
    }

    private String checkPre(String pre){
        String preStr = StringUtils.removeTrailingSpace(pre);
        for (;;){
            if (preStr.endsWith(",")){
                preStr = preStr.substring(0, preStr.length() - 1);
                preStr = StringUtils.removeTrailingSpace(preStr);
            }else break;
        }
        return preStr;
    }

    private String checkAft(String aft){
        String inStr = StringUtils.removeTrailingSpace(aft);
        for (;;){
            if (inStr.endsWith("and")){
                inStr = inStr.substring(0, inStr.lastIndexOf("and"));
                inStr = StringUtils.removeTrailingSpace(inStr);
            }else break;
        }
        for (;;){
            if (inStr.endsWith("or")){
                inStr = inStr.substring(0, inStr.lastIndexOf("or"));
                inStr = StringUtils.removeTrailingSpace(inStr);
            }else break;
        }
        String space = StringUtils.removeFrontSpace(inStr);
        for (;;){
            if (space.startsWith("and")){
                space = space.substring(3);
                space = StringUtils.removeFrontSpace(space);
            }else break;
        }
        for (;;){
            if (space.startsWith("or") && !space.startsWith("order")){
                space = space.substring(2);
                space = StringUtils.removeFrontSpace(space);
            }else break;
        }
        return space;
    }

    public String toPreString(){
        return pre.toString();
    }

    public String toAftString(){
        return aft.toString();
    }

    public boolean isAndCon() {
        return andCon;
    }

    public SqlOutStatement filp(){
        andCon = !andCon;
        return this;
    }

    private void start(){
         if (andCon){
            doAnd();
        }else doOr();
    }

    private void end(String cn, String val, boolean type){
        if (!type && val.contains("?")){
            addVariable(getWhereIndex(), cn, OperationType.SELECT);
        }
    }

    public SqlOutStatement writeWhere(){
        aft.append(" where ");
        return this;
    }

    public SqlOutStatement removeUpdateValue(String columnName){
        throw new UnsupportedOperationException("not a update statement");
    }

    public SqlOutStatement removeInsertValue(String columnName){
        throw new UnsupportedOperationException("not a insert statement");
    }

    public boolean repalceUpdateVariable(String columnName, String newVariable){
        throw new UnsupportedOperationException("not a update statement");
    }

    public boolean isSet(String columnName){
        throw new UnsupportedOperationException("not a update statement");
    }

    public SqlOutStatement writeSetVariable(String columnName, String variable){
        throw new UnsupportedOperationException("not a update statement");
    }

    public SqlOutStatement writeSet(String column, String value){
        return writeSet(column, value, true);
    }

    public SqlOutStatement writeSet(String column, String value, boolean type){
        throw new UnsupportedOperationException("not a update statement");
    }

    public Map<String, ValueAndType> getUpdateSetValueAndType(){
        throw new UnsupportedOperationException("not a update statement");
    }

    public Map<String, ValueAndType> getInsertValueAndType(){
        throw new UnsupportedOperationException("not a update statement");
    }


    public SqlOutStatement insertVariable(String columnName, String variable){
        throw new UnsupportedOperationException("not a insert statement");
    }

    public boolean repalceInsertVariable(String columnName, String newVariable){
        throw new UnsupportedOperationException("not a insert statement");
    }

    public SqlOutStatement insertValue(String column, String val){
        return insertValue(column, val, true);
    }

    public SqlOutStatement insertValue(String column, String val, boolean type){
        throw new UnsupportedOperationException("not a insert statement");
    }

    public SqlOutStatement writePre(String sql){
        if (!sql.startsWith(" ")){
            pre.append(" ");
        }
        pre.append(sql);
        return this;
    }

    public SqlOutStatement writeAftSeq(String sql){
        return writeAftSeq(sql, true);
    }

    public SqlOutStatement writeAftSeq(String sql, boolean and){
        if (shouldWriteConn()){
            writeAft((and ? "and " : "or ") + sql);
        }else {
            writeAft(sql);
        }
        return this;
    }

    public SqlOutStatement writeAft(String sql){
        if (StringUtils.hasText(sql)){
            sql = StringUtils.addIfNotStartWith(sql, " ");
            aft.append(sql);
        }
        return this;
    }

    public SqlOutStatement write(String sql){
        return write(sql, null);
    }

    public SqlOutStatement write(String sql, OperationType type){
        if (StringUtils.hasText(sql)){
            registerOperations(o ->{
                writeAft(o.getValue());
            }, NO_COLUMN_OPERATION, sql, false, type);
        }
        return this;
    }


    public SqlOutStatement writeLike(String column, String value){
        registerOperations(o -> {
            start();
            writeAft(getColumnName(o.getColumnName()));
            writeAft("like");
            String oValue = o.getValue();
            oValue = StringUtils.removeIfStartWith(oValue, "'");
            oValue = StringUtils.removeIfEndWith(oValue, "'");
            writeAft("'%" + JdbcSqlUtils.getEscapeString(oValue) + "%'");
            end(o.getColumnName(), o.getValue(), false);
        }, column, value, false, OperationType.SELECT);
        return this;
    }

    public SqlOutStatement writeNotIn(String column, String... value){
        return writeNotIn(column, true, value);
    }

    public SqlOutStatement writeNotIn(String column, boolean type, String... value){
        registerOperations( o -> {
            doWriteNotIn(getColumnName(o.getColumnName()), o.isType(), value);
        }, column, null, type, OperationType.SELECT);
        return this;
    }

    private void doWriteNotIn(String column, boolean type, String... value){
        start();
        writeAft(column);
        writeAft("not in");
        writeAft("(");
        ArrayUtils.stateFor(value, (s, last, first) -> {
            writeAft(getString(s, type, isAutoEscape()));
            if (!last)
                writeAft(",");
        });
        writeAft(")");
        for (String v : value) {
            end(column, v, type);
        }
    }

    public SqlOutStatement writeIn(String column, boolean type, Collection<String> collection){
        return writeIn(column, type, collection.toArray(new String[0]));
    }

    public SqlOutStatement writeIn(String column, String... value){
        return writeIn(column, true, value);
    }

    public SqlOutStatement writeIn(String column, boolean type, String... value){
        registerOperations( o -> {
            doWriteIn(getColumnName(o.getColumnName()), o.isType(), value);
        }, column, null, type, OperationType.SELECT);
        return this;
    }

    private void doWriteIn(String column, boolean type, String... value){
        start();
        writeAft(column);
        writeAft("in");
        writeAft("(");
        ArrayUtils.stateFor(value, (s, last, first) -> {
            writeAft(getString(s, type, isAutoEscape()));
            if (!last)
                writeAft(",");
        });
        writeAft(")");
        for (String v : value) {
            end(column, v, type);
        }
    }

    public SqlOutStatement or(){
        registerOperations(o ->{
            doOr();
        }, OperationType.SELECT);
        return this;
    }

    private void doOr(){
        String aftString = toAftString();
        String space = StringUtils.removeTrailingSpace(aftString);
        if (shouldWriteConn()){
            writeAft("or");
        }
    }

    public SqlOutStatement writeOr(Consumer<SqlOutStatement> consumer){
        SqlOutStatement statement = createStatement();
        statement.filp();
        consumer.accept(statement);
        registerOperations(o -> {
            doWriteOr(statement);
        }, null, null, false, OperationType.SELECT, statement);
        return this;
    }

    private void doWriteOr(SqlOutStatement statement){
        start();
        List<SqlVariable> sqlVariables = statement.variables;
        for (SqlVariable sqlVariable : sqlVariables) {
            sqlVariable.setIndex(sqlVariable.getIndex() + whereIndex);
        }
        whereIndex += sqlVariables.size();
        addAllVariable(sqlVariables);
        writeAft("(");
        writeAft(checkAft(statement.toAftString()));
        writeAft(")");
    }

    public SqlOutStatement and(){
        registerOperations(o -> {
            doAnd();
        }, OperationType.SELECT);
        return this;
    }

    private void doAnd(){
        String aftString = toAftString();
        String space = StringUtils.removeTrailingSpace(aftString);
        if (shouldWriteConn()){
            writeAft("and");
        }
    }

    public boolean shouldWriteConn(){
        String aftString = toAftString();
        String space = StringUtils.removeTrailingSpace(aftString);
        return !space.endsWith("and") && !space.endsWith("or");
    }

    public SqlOutStatement writeAnd(Consumer<SqlOutStatement> consumer){
        SqlOutStatement statement = createStatement();
        consumer.accept(statement);
        registerOperations(o -> {
            doWriteAnd(statement);
        }, null, null, false, OperationType.SELECT, statement);
        return this;
    }

    private void doWriteAnd(SqlOutStatement statement){
        start();
        List<SqlVariable> sqlVariables = statement.variables;
        for (SqlVariable sqlVariable : sqlVariables) {
            sqlVariable.setIndex(sqlVariable.getIndex() + whereIndex);
        }
        whereIndex += sqlVariables.size();
        addAllVariable(sqlVariables);
        writeAft("(");
        writeAft(checkAft(statement.toAftString()));
        writeAft(")");
    }

    protected SqlOutStatement createStatement(){
        return new SqlOutStatement();
    }

    public SqlOutStatement writeOrNotEqMap(Map<String, String> kvmap, boolean type){
        writeOr(i ->{
            kvmap.forEach((k, v) ->{
                writeNotEq(k, v, type);
            });
        });
        return this;
    }

    public SqlOutStatement writeAndNotEqMap(Map<String, String> kvmap, boolean type){
        writeAnd(i ->{
            kvmap.forEach((k, v) ->{
                writeNotEq(k, v, type);
            });
        });
        return this;
    }

    public SqlOutStatement writeOrEqMap(Map<String, String> kvmap, boolean type){
        writeOr(i ->{
            kvmap.forEach((k, v) ->{
                writeEq(k, v, type);
            });
        });
        return this;
    }

    public SqlOutStatement writeAndEqMap(Map<String, String> kvmap, boolean type){
        writeAnd(i ->{
            kvmap.forEach((k, v) ->{
                writeEq(k, v, type);
            });
        });
        return this;
    }

    public SqlOutStatement writeIsNull(String column){
        registerOperations( o -> {
            start();
            writeAft(getColumnName(o.getColumnName()));
            writeAft("is null");
        }, column, null, false, OperationType.SELECT);
        return this;
    }

    public SqlOutStatement writeIsNotNull(String column){
        registerOperations( o -> {
            start();
            writeAft(getColumnName(o.getColumnName()));
            writeAft("is not null");
        }, column, null, false, OperationType.SELECT);
        return this;
    }

    public SqlOutStatement writeNotEq(String column, String value){
        return writeNotEq(column, value, true);
    }

    public SqlOutStatement writeNotEq(String column, String value, boolean type){
        registerOperations( o -> {
            start();
            writeAft(getColumnName(o.getColumnName()));
            writeAft("<>");
            writeAft(getString(o.getValue(), o.isType(), isAutoEscape()));
            end(o.getColumnName(), o.getValue(), o.isType());
        }, column, value, type, OperationType.SELECT);
        return this;
    }

    public SqlOutStatement writeEq(String column, String value){
        return writeEq(column, value, true);
    }

    public SqlOutStatement writeEq(String column, String value, boolean type){
        registerOperations(o -> {
            start();
            writeAft(getColumnName(o.getColumnName()));
            writeAft("=");
            writeAft(getString(o.getValue(), o.isType(), isAutoEscape()));
            end(o.getColumnName(), o.getValue(), o.isType());
        }, column, value, type, OperationType.SELECT);
        return this;
    }

    public SqlOutStatement writeLastOrderByAsc(String... columnNames){
        if (columnNames.length != 0){
            writeLastSql("order by");
            writeLastSql(merge(columnNames) + " asc");
        }

        return this;
    }

    private String merge(String... cns){
        StringJoiner joiner = new StringJoiner(",");
        for (String cn : cns) {
            joiner.add(getColumnName(cn));
        }

        return joiner.toString();
    }

    public SqlOutStatement writeLastOrderByDesc(String... columnNames){
        if (columnNames.length != 0){
            writeLastSql("order by");
            writeLastSql(merge(columnNames) + " desc");
        }
        return this;
    }

    public SqlOutStatement writeLastSql(String sql){
        registerOperations(o -> {
            if (!lastSql.contains(o.getValue()))
                lastSql.add(o.getValue());
        }, null, sql, false, OperationType.SELECT);
        return this;
    }
}

