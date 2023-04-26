package com.black.core.sql.unc;

import com.black.core.query.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class InsertStatement extends SqlStatement{

    final StringBuilder values = new StringBuilder();

    int insertIndex = 0;

    int sort = 0;

    final Map<String, ValueAndType> valueAndTypeMap = new HashMap<>();

    public InsertStatement() {
    }

    public InsertStatement(String preStr, String aftStr) {
        super(preStr, aftStr);
    }

    @Override
    public Map<String, ValueAndType> getInsertValueAndType() {
        return valueAndTypeMap;
    }

    @Override
    public String toPreString() {
        return super.toPreString() + values.toString();
    }


    @Override
    public SqlStatement resetTableName(String newTableName) {
        int i = pre.indexOf("into");
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

    @Override
    public SqlStatement finish(){
        super.finish();
        values.append(" values ");
        writePre("(");
        values.append("(");
        ArrayUtils.sortMapStateFor(valueAndTypeMap, v ->{
            return v.sort;
        }, (k, v, f, l) ->{
            writePre(k);
            values.append(getString(v.val, v.type));
            if (!l){
                writePre(",");
                values.append(", ");
            }
        });
        writePre(")");
        values.append(")");
        if (insertIndex != 0){
            for (SqlVariable variable : variables) {
                if (variable.getOperationType() != OperationType.INSERT) {
                    variable.setIndex(variable.getIndex() + insertIndex);
                }
            }
        }
        return this;
    }



    @Override
    public SqlStatement insertVariable(String columnName, String variable) {
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                valueAndTypeMap.put(o.getColumnName(), new ValueAndType(o.getValue(), false, sort++));
                if (o.getValue().contains("?")){
                    SqlVariable sqlVariable = new SqlVariable(++insertIndex, o.getColumnName(), OperationType.INSERT);
                    variables.add(sqlVariable);
                }
            }, columnName, variable, false);
            operation.setOperationType(OperationType.INSERT);
            getOperations().add(operation);
            return this;
        }
        insertValue(columnName, variable, false);
        if (variable.contains("?")){
            SqlVariable sqlVariable = new SqlVariable(++insertIndex, columnName, OperationType.INSERT);
            variables.add(sqlVariable);
        }
        return this;
    }

    @Override
    public SqlStatement removeInsertValue(String columnName) {
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                doReomveInsertValue(o.getColumnName());
            }, columnName, null, false);
            operation.setOperationType(OperationType.INSERT);
            getOperations().add(operation);
            return this;
        }else doReomveInsertValue(columnName);
        return this;
    }

    private void doReomveInsertValue(String columnName){
        if (valueAndTypeMap.containsKey(columnName)) {
            valueAndTypeMap.remove(columnName);
            AtomicReference<SqlVariable> willDelete = new AtomicReference<>();
            variables.removeIf(variable -> {
                if (variable.getOperationType() == OperationType.INSERT && columnName.equals(variable.getColumnName())){
                    willDelete.set(variable);
                    return true;
                }
                return false;
            });
            if (willDelete.get() != null) {
                for (SqlVariable variable : variables) {
                    if (variable.getOperationType() == OperationType.INSERT){
                        if (variable.getIndex() > willDelete.get().getIndex()){
                            variable.setIndex(variable.getIndex() -1);
                        }
                    }
                }
                insertIndex --;
            }
        }
    }

    @Override
    public boolean repalceInsertVariable(String columnName, String newVariable){
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                if (valueAndTypeMap.containsKey(o.getColumnName())) {
                    ValueAndType valueAndType = valueAndTypeMap.get(o.getColumnName());
                    valueAndType.val = o.getValue();
                }
            }, columnName, newVariable, false);
            operation.setOperationType(OperationType.INSERT);
            getOperations().add(operation);
            return true;
        }
        if (valueAndTypeMap.containsKey(columnName)) {
            ValueAndType valueAndType = valueAndTypeMap.get(columnName);
            valueAndType.val = newVariable;
            return true;
        }
        return false;
    }

    @Override
    public SqlStatement insertValue(String column, String val, boolean type){
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                valueAndTypeMap.put(o.getColumnName(), new ValueAndType(o.getValue(), false, sort++));
            }, column, val, false);
            operation.setOperationType(OperationType.INSERT);
            getOperations().add(operation);
            return this;
        }
        valueAndTypeMap.put(column, new ValueAndType(val, type, sort++));
        return this;
    }

    @Override
    protected SqlStatement createStatement() {
        return new InsertStatement();
    }

}
