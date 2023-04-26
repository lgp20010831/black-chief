package com.black.core.sql.unc;

import com.black.core.query.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateStatement extends SqlStatement{

    int updateIndex = 0;

    int sort = 0;

    final Map<String, ValueAndType> valueAndTypeMap = new HashMap<>();

    public UpdateStatement() {
    }

    public UpdateStatement(String preStr, String aftStr) {
        super(preStr, aftStr);
    }

    @Override
    public Map<String, ValueAndType> getUpdateSetValueAndType() {
        return valueAndTypeMap;
    }

    @Override
    public String toString() {
       return super.toString();
    }


    @Override
    public boolean isSet(String columnName) {
        return valueAndTypeMap.containsKey(columnName);
    }

    @Override
    public SqlStatement resetTableName(String newTableName) {
        int i = pre.indexOf("update");
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
        return this;    }

    @Override
    public SqlStatement finish(){
        super.finish();
        ArrayUtils.sortMapStateFor(valueAndTypeMap, v -> v.sort, (k, v, f, l) ->{
            writePre(k);
            writePre("=");
            writePre(getString(v.val, v.type));
            if (!l){
                writePre(",");
            }
        });
        if (updateIndex != 0){
            for (SqlVariable variable : variables) {
                if (variable.getOperationType() != OperationType.UPDATE) {
                    variable.setIndex(variable.getIndex() + updateIndex);
                }
            }
        }
        return this;
    }


    @Override
    public SqlStatement writeSetVariable(String columnName, String variable) {
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                valueAndTypeMap.put(o.getColumnName(), new ValueAndType(o.getValue(), o.isType(), sort++));
                if (o.getValue().contains("?")){
                    SqlVariable sqlVariable = new SqlVariable(++updateIndex, o.getColumnName(), OperationType.UPDATE);
                    variables.add(sqlVariable);
                }
            }, columnName, variable, false);
            operation.setOperationType(OperationType.UPDATE);
            getOperations().add(operation);
        }else {
            writeSet(columnName, variable, false);
            if (variable.contains("?")){
                SqlVariable sqlVariable = new SqlVariable(++updateIndex, columnName, OperationType.UPDATE);
                variables.add(sqlVariable);
            }
        }

        return this;
    }

    @Override
    public SqlStatement removeUpdateValue(String columnName) {
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                doReomveUpdateValue(columnName);
            }, columnName, null, false);
            operation.setOperationType(OperationType.UPDATE);
            getOperations().add(operation);
        }else doReomveUpdateValue(columnName);
        return this;
    }

    private void doReomveUpdateValue(String columnName){
        if (valueAndTypeMap.containsKey(columnName)) {
            valueAndTypeMap.remove(columnName);
            AtomicReference<SqlVariable> willDelete = new AtomicReference<>();
            variables.removeIf(variable -> {
                if (variable.getOperationType() == OperationType.UPDATE && columnName.equals(variable.getColumnName())){
                    willDelete.set(variable);
                    return true;
                }
                return false;
            });
            if (willDelete.get() != null) {
                for (SqlVariable variable : variables) {
                    if (variable.getOperationType() == OperationType.UPDATE){
                        if (variable.getIndex() > willDelete.get().getIndex()){
                            variable.setIndex(variable.getIndex() -1);
                        }
                    }
                }
                updateIndex --;
            }
        }
    }

    @Override
    public boolean repalceUpdateVariable(String columnName, String newVariable) {
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                if (valueAndTypeMap.containsKey(o.getColumnName())) {
                    ValueAndType valueAndType = valueAndTypeMap.get(o.getColumnName());
                    valueAndType.val = o.getValue();
                }
            }, columnName, newVariable, false);
            operation.setOperationType(OperationType.UPDATE);
            getOperations().add(operation);
        }else {
            if (valueAndTypeMap.containsKey(columnName)) {
                ValueAndType valueAndType = valueAndTypeMap.get(columnName);
                valueAndType.val = newVariable;
                return true;
            }
        }
        return false;
    }

    @Override
    public SqlStatement writeSet(String column, String value, boolean type) {
        if (isRetain()){
            Operation operation = new Operation(sort++, o -> {
                valueAndTypeMap.put(o.getColumnName(), new ValueAndType(o.getValue(), o.isType(), sort++));
            }, column, value, false);
            operation.setOperationType(OperationType.UPDATE);
            getOperations().add(operation);
        }else
        valueAndTypeMap.put(column, new ValueAndType(value, type, sort++));
        return this;
    }

    @Override
    protected SqlStatement createStatement() {
        return new UpdateStatement();
    }
}
