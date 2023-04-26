package com.black.sql;

import com.black.core.sql.unc.OperationType;
import com.black.core.sql.unc.SqlVariable;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractStatement {

    protected static final String NO_COLUMN_OPERATION = "NO_COLUMN_OPERATION$$";

    //缓存所有操作
    protected List<Operation> operations = new ArrayList<>();

    protected volatile boolean flushing = false;

    protected Map<OperationType, Map<String, List<Operation>>> indexOperationCache = new HashMap<>();

    //对于 sql 变量进行记录, 比如 id = ?, name = ?
    //则记录两个变量(主要记录下标, 列名, 操作类型)
    protected final List<SqlVariable> variables = new ArrayList<>();

    protected Map<OperationType, Map<String, List<SqlVariable>>> indexVariableCache = new HashMap<>();

    protected AbstractStatement registerOperations(Consumer<Operation> consumer,
                                                 OperationType operationType){
        return registerOperations(consumer, null, null, false, operationType, null);
    }


    protected AbstractStatement registerOperations(Consumer<Operation> consumer,
                                                 String column,
                                                 String value,
                                                 boolean type,
                                                 OperationType operationType){
        return registerOperations(consumer, column, value, type, operationType, null);
    }

    protected AbstractStatement registerOperations(Consumer<Operation> consumer,
                                                 String column,
                                                 String value,
                                                 boolean type,
                                                 OperationType operationType,
                                                 SqlOutStatement statement){
        Operation operation = new Operation(consumer, column = column == null ? NO_COLUMN_OPERATION : column, value, type);
        operation.setOperationType(operationType);
        operation.setOthereStatement(statement);
        if (flushing){
            invokeOpeation(operation);
        }else {
            operations.add(operation);
            indexOperationCache.computeIfAbsent(operationType, ot -> new HashMap<>())
                    .computeIfAbsent(column, cm -> new ArrayList<>()).add(operation);
        }

        return this;
    }

    protected void invokeOpeation(Operation operation){
        if(operation.getOthereStatement() != null){
            operation.getOthereStatement().flush();
        }
        operation.getConsumer().accept(operation);
    }

    protected AbstractStatement addAllVariable(List<SqlVariable> sqlVariables){
        variables.addAll(sqlVariables);
        for (SqlVariable sqlVariable : sqlVariables) {
            indexVariableCache.computeIfAbsent(sqlVariable.getOperationType(), ty -> new HashMap<>())
                    .computeIfAbsent(sqlVariable.getColumnName(), cm -> new ArrayList<>())
                    .add(sqlVariable);
        }
        return this;
    }

    protected AbstractStatement addVariable(int index, String column, OperationType type){
        SqlVariable variable = new SqlVariable(index, column, type);
        variables.add(variable);
        indexVariableCache.computeIfAbsent(type, ty -> new HashMap<>()).computeIfAbsent(column, cm -> new ArrayList<>()).add(variable);
        return this;
    }

    public boolean exisOperation(String columnName, OperationType operationType){
        Map<String, List<Operation>> listMap = indexOperationCache.get(operationType);
        if (listMap != null){
            return listMap.containsKey(columnName);
        }
        return false;
    }

    public Operation getSingleOperation(String columnName, OperationType operationType){
        List<Operation> operations = getOperation(columnName, operationType);
        return operations.isEmpty() ? null : operations.get(0);
    }

    public List<Operation> getOperation(String columnName, OperationType operationType){
        Map<String, List<Operation>> listMap = indexOperationCache.get(operationType);
        if (listMap != null){
            return listMap.getOrDefault(columnName, new ArrayList<>());
        }
        return new ArrayList<>();
    }

    public AbstractStatement replaceOperation(String columnName, OperationType operationType, String val, boolean type){
        for (Operation operation : operations) {
            if (columnName.equals(operation.getColumnName()) && operationType == operation.getOperationType()){
                operation.setValue(val);
                operation.setType(type);
            }
            if (operation.getOthereStatement() != null){
                operation.getOthereStatement().replaceOperation(columnName, operationType, val, type);
            }
        }
        return this;
    }

    public AbstractStatement removeOperation(String columnName, OperationType operationType){
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
        return this;
    }

    public AbstractStatement removeLastOperation(){
        if (!operations.isEmpty()){
            operations.remove(operations.size() - 1);
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
        Map<String, List<SqlVariable>> listMap = indexVariableCache.get(operationType);
        if (listMap != null){
            return listMap.getOrDefault(columnName, new ArrayList<>());
        }
        return new ArrayList<>();
    }

    public List<SqlVariable> getVariables(OperationType type){
        List<SqlVariable> sqlVariableList = new ArrayList<>();
        Map<String, List<SqlVariable>> listMap = indexVariableCache.get(type);
        if (listMap != null){
            for (List<SqlVariable> list : listMap.values()) {
                sqlVariableList.addAll(list);
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

    public List<SqlVariable> getVariables() {
        return variables;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
