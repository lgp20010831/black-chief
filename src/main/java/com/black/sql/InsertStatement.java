package com.black.sql;

import com.black.core.query.ArrayUtils;
import com.black.core.sql.unc.*;
import com.black.core.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.black.sql.JdbcSqlUtils.getString;

public class InsertStatement extends SqlOutStatement {

    StringBuilder values = new StringBuilder();

    StringBuilder insertColumn = new StringBuilder();

    int sort = 0;

    final Map<String, ValueAndType> valueAndTypeMap = new HashMap<>();

    public InsertStatement(){
        this("", "");
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
        return StringUtils.linkStr(super.toPreString(), " (",
                StringUtils.removeIfStartWith(insertColumn.toString(), ","), ") ", " values (",
                StringUtils.removeIfStartWith(values.toString(), ","), ")");
    }

    @Override
    public SqlOutStatement close() {
        super.close();
        values = new StringBuilder();
        insertColumn = new StringBuilder();
        sort = 0;
        valueAndTypeMap.clear();
        return this;
    }

    @Override
    public SqlOutStatement flush(){
        super.flush();
        try {
            ArrayUtils.sortMapStateFor(valueAndTypeMap, ValueAndType::getSort, (k, v, f, l) ->{
                insertColumn.append(",");
                insertColumn.append(k);
                values.append(",");
                values.append(getString(v.getVal(), v.isType(), isAutoEscape()));
            });
            if (!variables.isEmpty()){
                for (SqlVariable variable : variables) {
                    if (variable.getOperationType() != OperationType.INSERT) {
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


    private int getExist(){
        return getVariables(OperationType.INSERT).size();
    }

    private void doWirteInsert(Operation o){
        valueAndTypeMap.put(o.getColumnName(), new ValueAndType(o.getValue(), o.isType(), sort++));
        if (!o.isType() && o.getValue().contains("?")){
            addVariable(getExist() + 1, o.getColumnName(), OperationType.INSERT);
        }
    }

    @Override
    public SqlOutStatement insertVariable(String columnName, String variable) {
        registerOperations(this::doWirteInsert, columnName, variable, false, OperationType.INSERT);
        return this;
    }

    @Override
    public SqlOutStatement removeInsertValue(String columnName) {
        registerOperations(o -> {
            doReomveInsertValue(o.getColumnName());
        }, columnName, null, false, OperationType.INSERT);
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
            }
        }
    }

    @Override
    public boolean repalceInsertVariable(String columnName, String newVariable){
        registerOperations(o -> {
            if (valueAndTypeMap.containsKey(o.getColumnName())) {
                ValueAndType valueAndType = valueAndTypeMap.get(o.getColumnName());
                valueAndType.setVal(o.getValue());
            }
        }, columnName, newVariable, false, OperationType.INSERT);
        return false;
    }

    @Override
    public SqlOutStatement insertValue(String column, String val, boolean type){
        registerOperations(this::doWirteInsert, column, val, type, OperationType.INSERT);
        return this;
    }

    @Override
    protected SqlOutStatement createStatement() {
        return new InsertStatement();
    }

}
