package com.black.sql;

import com.black.core.query.ArrayUtils;
import com.black.core.sql.unc.*;
import com.black.core.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.black.sql.JdbcSqlUtils.getString;

public class UpdateStatement extends SqlOutStatement{

    int sort = 0;

    StringBuilder set = new StringBuilder();

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
    public boolean isSet(String columnName) {
        return valueAndTypeMap.containsKey(columnName);
    }

    @Override
    public SqlOutStatement close() {
        super.close();
        sort = 0;
        set = new StringBuilder();
        valueAndTypeMap.clear();
        return this;
    }

    public SqlOutStatement flush(){
        super.flush();
        try {
            ArrayUtils.sortMapStateFor(valueAndTypeMap, ValueAndType::getSort, (k, v, f, l) ->{
                set.append(",");
                set.append(k);
                set.append("=");
                set.append(getString(v.getVal(), v.isType(), isAutoEscape()));
            });
            if (!variables.isEmpty()){
                for (SqlVariable variable : variables) {
                    if (variable.getOperationType() != OperationType.UPDATE) {
                        variable.setIndex(variable.getIndex() + getExist());
                    }
                }
                whereIndex += getExist();
            }
        }finally {
            sort = 0;
            valueAndTypeMap.clear();
        }
        return this;
    }


    @Override
    public String toPreString() {
        String setStr = set.toString();
        return StringUtils.linkStr(super.toPreString(), " ", StringUtils.removeIfStartWith(setStr, ","));
    }

    private int getExist(){
        return getVariables(OperationType.UPDATE).size();
    }


    @Override
    public SqlOutStatement writeSetVariable(String columnName, String variable) {
        registerOperations(this::doWriteSet, columnName, variable, false, OperationType.UPDATE);
        return this;
    }

    private void doWriteSet(Operation o){
        valueAndTypeMap.put(o.getColumnName(), new ValueAndType(o.getValue(), o.isType(), sort++));
        if (!o.isType() && o.getValue().contains("?")){
            addVariable(getExist() + 1, o.getColumnName(), OperationType.UPDATE);
        }
    }

    @Override
    public SqlOutStatement removeUpdateValue(String columnName) {
        registerOperations(o -> {
            doReomveUpdateValue(columnName);
        }, columnName, null, false, OperationType.UPDATE);
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
            Map<String, List<SqlVariable>> map = indexVariableCache.get(OperationType.UPDATE);
            if (map != null){
                map.remove(columnName);
            }
            if (willDelete.get() != null) {
                for (SqlVariable variable : variables) {
                    if (variable.getOperationType() == OperationType.UPDATE){
                        if (variable.getIndex() > willDelete.get().getIndex()){
                            variable.setIndex(variable.getIndex() -1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean repalceUpdateVariable(String columnName, String newVariable) {
        registerOperations(o -> {
            if (valueAndTypeMap.containsKey(o.getColumnName())) {
                ValueAndType valueAndType = valueAndTypeMap.get(o.getColumnName());
                valueAndType.setVal(o.getValue());
            }
        }, columnName, newVariable, false, OperationType.UPDATE);
        return false;
    }

    @Override
    public SqlOutStatement writeSet(String column, String value, boolean type) {
        registerOperations(this::doWriteSet, column, value, type, OperationType.UPDATE);
        return this;
    }

    @Override
    protected SqlOutStatement createStatement() {
        return new UpdateStatement();
    }
}
