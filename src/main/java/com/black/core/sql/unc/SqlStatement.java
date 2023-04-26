package com.black.core.sql.unc;

import com.black.core.annotation.Important;
import com.black.core.query.ArrayUtils;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StringUtils;
import com.black.table.TableMetadata;

import java.util.*;
import java.util.function.Consumer;

@Important
@SuppressWarnings("all")
public class SqlStatement {


    public static final String NO_COLUMN_OPERATION = "NO_COLUMN_OPERATION$$";

    protected final StringBuilder pre;

    /**
     * 该string buildr 主要缓存条件 sql 语句
     */
    protected final StringBuilder aft;

    /**
     * 保留操作属性, 默认为 false
     * 当该值为 true 时, 所有的操作都会被等装成
     * {@link Operation} 对象, 并缓存, 不会立即渲染到最终
     * 的sql上, 所有中途可以对之前的操作进行替换和删除
     * 如 在某一刻 id 为不确定值 则 id = ?
     * 但是之后需要将 id 进行修改, 则需要开启此功能
     */
    private boolean retain = false;


    private boolean finish = false;

    /**
     * 每次添加条件之间连接符是否用 and, 如果为 false 则用 or
     * 替换方法调用 filp 方法
     */
    private boolean andCon = true;

    private int sort = 0;

    private int whereIndex = 0;

    //缓存所有操作
    private List<Operation> operations = new ArrayList<>();

    //对于 sql 变量进行记录, 比如 id = ?, name = ?
    //则记录两个变量(主要记录下标, 列名, 操作类型)
    protected final List<SqlVariable> variables = new ArrayList<>();


    protected final List<String> lastSql = new ArrayList<>();

    public SqlStatement(){
        pre = new StringBuilder();
        aft = new StringBuilder();
    }

    public SqlStatement(String preStr, String aftStr){
        pre = new StringBuilder(preStr);
        aft = new StringBuilder(aftStr);
    }

    public static String getString(String str, boolean stringType){
        return stringType ? "'" + str + "'" : str;
    }

    public int getWhereIndex() {
        return ++whereIndex;
    }


    public SqlStatement resetTableName(String newTableName){
        int i = pre.indexOf("from");
        if (i != -1){
            int len = 0;
            int s = i;
            boolean start = false;
            for (;;){
                if (i >= pre.length()) break;
                char c = pre.charAt(i);
                if (c != ' ' && start){len++;}
                if (c == ' ' && !start){start = true; s = i + 1;}
                else if (c == ' ' && start)break;
                i++;
            }
            pre.replace(s, s + len, newTableName);
        }
        return this;
    }

    public SqlStatement calibration(TableMetadata tableMetadata){
        return calibration(tableMetadata, getOperations());
    }

    public SqlStatement calibration(TableMetadata tableMetadata, List<Operation> operations){
        if (!retain) return this;
        Set<String> columnNameSet = tableMetadata.getColumnNameSet();
        operations.removeIf(o -> {
            SqlStatement othereStatement = o.getOthereStatement();
            if(othereStatement != null){
                calibration(tableMetadata, othereStatement.getOperations());
                return othereStatement.getOperations().size() == 0;
            }else {
                return !columnNameSet.contains(o.getColumnName());
            }

        });
        return this;
    }

    public Map<String, Map<OperationType, Set<SqlVariable>>> group(){
        Map<String, Map<OperationType, Set<SqlVariable>>> map = new HashMap<>();
        List<SqlVariable> variables = getVariables();
        for (SqlVariable variable : variables) {
            Map<OperationType, Set<SqlVariable>> typeSetMap = map.computeIfAbsent(variable.getColumnName(), n -> new HashMap<>());
            Set<SqlVariable> sqlVariables = typeSetMap.computeIfAbsent(variable.getOperationType(), t -> new HashSet<>());
            sqlVariables.add(variable);
        }
        return map;
    }

    @Override
    public String toString() {
        String toAftString = checkAft(StringUtils.removeFrontSpace(toAftString()));
        String preString = checkPre(StringUtils.removeFrontSpace(toPreString()));
        return StringUtils.linkStr(preString,
                StringUtils.hasText(toAftString) ? " where " + toAftString : "");
    }

    public String getCheckAft(){
        return checkAft(StringUtils.removeFrontSpace(toAftString()));
    }

    public String getCheckPre(){
        return checkPre(StringUtils.removeFrontSpace(toPreString()));
    }

    public boolean isRetain() {
        return retain;
    }

    public boolean isFinish() {
        return finish;
    }

    public List<SqlVariable> getVariables() {
        return variables;
    }

    public SqlStatement openRetain(){
        retain = true;
        return this;
    }

    public SqlStatement closeRetain(){
        retain = false;
        return this;
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

    public SqlStatement filp(){
        andCon = !andCon;
        return this;
    }

    private void start(){
         if (andCon){
            writeAft("and");
        }else writeAft("or");
    }

    private void end(String cn, String val, boolean type){
        if (!type && val.contains("?")){
            variables.add(new SqlVariable(getWhereIndex(), cn, OperationType.SELECT));
        }
    }

    public SqlStatement writeWhere(){
        aft.append(" where ");
        return this;
    }

    public SqlStatement removeUpdateValue(String columnName){
        throw new UnsupportedOperationException("not a update statement");
    }

    public SqlStatement removeInsertValue(String columnName){
        throw new UnsupportedOperationException("not a insert statement");
    }

    public boolean repalceUpdateVariable(String columnName, String newVariable){
        throw new UnsupportedOperationException("not a update statement");
    }

    public boolean isSet(String columnName){
        throw new UnsupportedOperationException("not a update statement");

    }

    public SqlStatement writeSetVariable(String columnName,  String variable){
        throw new UnsupportedOperationException("not a update statement");
    }

    public SqlStatement  writeSet(String column, String value){
        return writeSet(column, value, true);
    }

    public SqlStatement writeSet(String column, String value, boolean type){
        throw new UnsupportedOperationException("not a update statement");
    }

    public Map<String, ValueAndType> getUpdateSetValueAndType(){
        throw new UnsupportedOperationException("not a update statement");
    }

    public Map<String, ValueAndType> getInsertValueAndType(){
        throw new UnsupportedOperationException("not a update statement");
    }


    @Important // 不要重复调用
    public SqlStatement finish(){
        if (finish) return this;
        try {
            for (Operation operation : operations) {
                if(operation.getOthereStatement() != null){
                    operation.getOthereStatement().finish();
                }
                operation.getConsumer().accept(operation);
            }
            for (String sql : lastSql) {
                writeAft(sql);
            }
            return this;
        }finally {
            operations.clear();
            finish = true;
        }
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public SqlStatement reset(){
        if (retain){
            operations.clear();
        }

        return this;
    }

    public boolean exisOperation(String columnName, OperationType operationType){
        for (Operation operation : operations) {
            if (columnName.equals(operation.getColumnName()) && operationType == operation.getOperationType()){
                return true;
            }
        }
        return false;
    }


    public SqlStatement replaceOperation(String columnName, OperationType operationType, String val, boolean type){
        if(retain){
            for (Operation operation : operations) {
                if (columnName.equals(operation.getColumnName()) && operationType == operation.getOperationType()){
                    operation.setValue(val);
                    operation.setType(type);
                }
                if (operation.getOthereStatement() != null){
                    operation.getOthereStatement().replaceOperation(columnName, operationType, val, type);
                }
            }
        }
        return this;
    }

    public SqlStatement removeOperation(String columnName, OperationType operationType){
        if(retain){
            Iterator<Operation> iterator = operations.iterator();
            while (iterator.hasNext()) {
                Operation operation = iterator.next();
                if (columnName.equals(operation.getColumnName()) && operationType == operation.getOperationType()){
                    iterator.remove();
                }
                if (operation.getOthereStatement() != null){
                    operation.getOthereStatement().removeOperation(columnName, operationType);
                }
            }
        }
        return this;
    }


    public SqlVariable getSingleVariable(String columnName, OperationType operationType){
        List<SqlVariable> sqlVariables = getVariable(columnName, operationType);
        return sqlVariables.isEmpty() ? null : sqlVariables.get(0);
    }

    public SqlVariable getSingleVariable(String columnName){
        List<SqlVariable> sqlVariables = getVariables(columnName);
        return sqlVariables.isEmpty() ? null : sqlVariables.get(0);
    }


    public List<SqlVariable> getVariable(String columnName, OperationType operationType){
        List<SqlVariable> sqlVariableList = new ArrayList<>();
        for (SqlVariable variable : variables) {
            if (columnName.equals(variable.getColumnName()) && variable.getOperationType() == operationType){
                sqlVariableList.add(variable);
            }
        }
        return sqlVariableList;
    }

    public List<SqlVariable> getVariables(OperationType type){
        List<SqlVariable> sqlVariableList = new ArrayList<>();
        for (SqlVariable variable : variables) {
            if (variable.getOperationType() == type){
                sqlVariableList.add(variable);
            }
        }
        return sqlVariableList;
    }

    public List<SqlVariable> getVariables(String columnName){
        List<SqlVariable> sqlVariableList = new ArrayList<>();
        for (SqlVariable variable : variables) {
            if (columnName.equals(variable.getColumnName())){
                sqlVariableList.add(variable);
            }
        }
        return sqlVariableList;
    }


    public SqlStatement insertVariable(String columnName, String variable){
        throw new UnsupportedOperationException("not a insert statement");
    }

    public boolean repalceInsertVariable(String columnName, String newVariable){
        throw new UnsupportedOperationException("not a insert statement");
    }

    public SqlStatement insertValue(String column, String val){
        return insertValue(column, val, true);
    }

    public SqlStatement insertValue(String column, String val, boolean type){
        throw new UnsupportedOperationException("not a insert statement");
    }

    public SqlStatement writePre(String sql){
        if (!sql.startsWith(" ")){
            pre.append(" ");
        }
        pre.append(sql);
        return this;
    }

    public SqlStatement writeAft(String sql){
        if (sql == null){return this;}
        if (!sql.startsWith(" ")){
            aft.append(" ");
        }
        aft.append(sql);
        return this;
    }

    public SqlStatement write(String sql){
        return write(sql, null);
    }

    public SqlStatement write(String sql, OperationType type){
        if (sql == null) return this;
        if (retain){
            Operation operation = new Operation(sort++, o -> {
              writeAft(o.getValue());
            }, NO_COLUMN_OPERATION, sql, false);
            operation.setOperationType(type);
            operations.add(operation);
        }else
            writeAft(sql);
        return this;
    }


    public SqlStatement writeLike(String column, String value){
        if (retain){
            Operation operation = new Operation(sort++, o -> {
                start();
                writeAft(o.getColumnName());
                writeAft("like");
                writeAft("'%" + o.getValue() + "%'");
                end(o.getColumnName(), o.getValue(), false);
            }, column, value, false);
            operation.setOperationType(OperationType.SELECT);
            operations.add(operation);
            return this;
        }
        start();
        writeAft(column);
        writeAft("like");
        writeAft("'%" + value + "%'");
        end(column, value, false);
        return this;
    }

    public SqlStatement writeNotIn(String column, String... value){
        return writeNotIn(column, true, value);
    }

    public SqlStatement writeNotIn(String column, boolean type, String... value){
        if (retain){
            Operation operation = new Operation(sort++, o -> {
                doWriteNotIn(o.getColumnName(), o.isType(), value);
            }, column, null, type);
            operation.setOperationType(OperationType.SELECT);
            operations.add(operation);
            return this;
        }
        doWriteNotIn(column, type, value);
        return this;
    }

    private void doWriteNotIn(String column, boolean type, String... value){
        start();
        writeAft(column);
        writeAft("not in");
        writeAft("(");
        ArrayUtils.stateFor(value, (s, last, first) -> {
            writeAft(getString(s, type));
            if (!last)
                writeAft(",");
        });
        writeAft(")");
        for (String v : value) {
            end(column, v, type);
        }
    }

    public SqlStatement writeIn(String column, boolean type, Collection<String> collection){
        return writeIn(column, type, collection.toArray(new String[0]));
    }

    public SqlStatement writeIn(String column, String... value){
        return writeIn(column, true, value);
    }

    public SqlStatement writeIn(String column, boolean type, String... value){
        if (retain){
            Operation operation = new Operation(sort++, o -> {
                doWriteIn(o.getColumnName(), o.isType(), value);
            }, column, null, type);
            operation.setOperationType(OperationType.SELECT);
            operations.add(operation);
            return this;
        }
        doWriteIn(column, type, value);
        return this;
    }

    private void doWriteIn(String column, boolean type, String... value){
        start();
        writeAft(column);
        writeAft("in");
        writeAft("(");
        ArrayUtils.stateFor(value, (s, last, first) -> {
            writeAft(getString(s, type));
            if (!last)
                writeAft(",");
        });
        writeAft(")");
        for (String v : value) {
            end(column, v, type);
        }
    }

    public SqlStatement or(){
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                writeAft("or");
            }, NO_COLUMN_OPERATION, null, false);
            operation.setOperationType(OperationType.SELECT);
            operations.add(operation);
        }else
        writeAft("or");
        return this;
    }

    public SqlStatement writeOr(Consumer<SqlStatement> consumer){
        if (retain){
            SqlStatement statement = createStatement();
            statement.openRetain();
            statement.filp();
            consumer.accept(statement);
            Operation operation = new Operation(sort++, o -> {
                doWriteOr(statement);
            }, NO_COLUMN_OPERATION, null, false);
            operation.setOperationType(OperationType.SELECT);
            operation.setOthereStatement(statement);
            operations.add(operation);
            return this;
        }
        SqlStatement statement = createStatement();
        statement.filp();
        consumer.accept(statement);
        doWriteOr(statement);
        return this;
    }

    private void doWriteOr(SqlStatement statement){
        start();
        List<SqlVariable> sqlVariables = statement.variables;
        for (SqlVariable sqlVariable : sqlVariables) {
            sqlVariable.setIndex(sqlVariable.getIndex() + whereIndex);
        }
        whereIndex += sqlVariables.size();
        variables.addAll(sqlVariables);
        writeAft("(");
        writeAft(checkAft(statement.toAftString()));
        writeAft(")");
    }

    public SqlStatement and(){
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                writeAft("and");
            }, NO_COLUMN_OPERATION);
            operation.setOperationType(OperationType.SELECT);
            operations.add(operation);
        }else
            writeAft("and");
        return this;
    }

    public SqlStatement writeAnd(Consumer<SqlStatement> consumer){
        if (retain){
            SqlStatement statement = createStatement();
            statement.openRetain();
            consumer.accept(statement);
            Operation operation = new Operation(sort++, o -> {
                doWriteAnd(statement);
            }, NO_COLUMN_OPERATION);
            operation.setOperationType(OperationType.SELECT);
            operation.setOthereStatement(statement);
            operations.add(operation);
            return this;
        }
        SqlStatement statement = createStatement();
        consumer.accept(statement);
        doWriteAnd(statement);
        return this;
    }

    private void doWriteAnd(SqlStatement statement){
        start();
        List<SqlVariable> sqlVariables = statement.variables;
        for (SqlVariable sqlVariable : sqlVariables) {
            sqlVariable.setIndex(sqlVariable.getIndex() + whereIndex);
        }
        whereIndex += sqlVariables.size();
        variables.addAll(sqlVariables);
        writeAft("(");
        writeAft(checkAft(statement.toAftString()));
        writeAft(")");
    }

    protected SqlStatement createStatement(){
        return new SqlStatement();
    }

    public SqlStatement writeOrNotEqMap(Map<String, String> kvmap, boolean type){
        writeOr(i ->{
            kvmap.forEach((k, v) ->{
                writeNotEq(k, v, type);
            });
        });
        return this;
    }

    public SqlStatement writeAndNotEqMap(Map<String, String> kvmap, boolean type){
        writeAnd(i ->{
            kvmap.forEach((k, v) ->{
                writeNotEq(k, v, type);
            });
        });
        return this;
    }

    public SqlStatement writeOrEqMap(Map<String, String> kvmap, boolean type){
        writeOr(i ->{
            kvmap.forEach((k, v) ->{
                writeEq(k, v, type);
            });
        });
        return this;
    }

    public SqlStatement writeAndEqMap(Map<String, String> kvmap, boolean type){
        writeAnd(i ->{
            kvmap.forEach((k, v) ->{
                writeEq(k, v, type);
            });
        });
        return this;
    }

    public SqlStatement writeIsNull(String column){
        if (retain){
            Operation operation = new Operation(sort++, o -> {
                start();
                writeAft(o.getColumnName());
                writeAft("is null");
            }, column, null, false);
            operation.setOperationType(OperationType.SELECT);
            operations.add(operation);
            return this;
        }
        start();
        writeAft(column);
        writeAft("is null");
        return this;
    }

    public SqlStatement writeIsNotNull(String column){
        if (retain){
            Operation operation = new Operation(sort++, o -> {
                start();
                writeAft(o.getColumnName());
                writeAft("is not null");
            }, column, null, false);
            operation.setOperationType(OperationType.SELECT);
            operations.add(operation);
            return this;
        }
        start();
        writeAft(column);
        writeAft("is not null");
        return this;
    }

    public SqlStatement writeNotEq(String column, String value){
        return writeNotEq(column, value, true);
    }

    public SqlStatement writeNotEq(String column, String value, boolean type){
        if (retain){
            Operation operation = new Operation(sort++, o -> {
                start();
                writeAft(o.getColumnName());
                writeAft("<>");
                writeAft(getString(o.getValue(), o.isType()));
                end(o.getColumnName(), o.getValue(), o.isType());
            }, column, value, type);
            operation.setOperationType(OperationType.SELECT);
            operations.add(operation);
            return this;
        }
        start();
        writeAft(column);
        writeAft("<>");
        writeAft(getString(value, type));
        end(column, value, type);
        return this;
    }

    public SqlStatement writeEq(String column, String value){
        return writeEq(column, value, true);
    }

    public SqlStatement writeEq(String column, String value, boolean type){
        if (retain){
            Operation operation = new Operation(sort++, o -> {
                start();
                writeAft(o.getColumnName());
                writeAft("=");
                writeAft(getString(o.getValue(), o.isType()));
                end(o.getColumnName(), o.getValue(), o.isType());
            }, column, value, type);
            operation.setOperationType(OperationType.SELECT);
            operations.add(operation);
            return this;
        }
        start();
        writeAft(column);
        writeAft("=");
        writeAft(getString(value, type));
        end(column, value, type);
        return this;
    }

    public SqlStatement writeLastOrderByAsc(String... columnNames){
        if (columnNames.length != 0){
            writeLastSql("order by");
            writeLastSql(SQLUtils.merge(columnNames) + " asc");
        }

        return this;
    }

    public SqlStatement writeLastOrderByDesc(String... columnNames){
        if (columnNames.length != 0){
            writeLastSql("order by");
            writeLastSql(SQLUtils.merge(columnNames) + " desc");
        }
        return this;
    }

    public SqlStatement writeLastSql(String sql){
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                if (!lastSql.contains(o.getValue()))
                    lastSql.add(o.getValue());
            }, null, sql, false);
            operation.setOperationType(OperationType.SELECT);
            operations.add(operation);
        }else {
            lastSql.add(sql);
        }
        return this;
    }
}

