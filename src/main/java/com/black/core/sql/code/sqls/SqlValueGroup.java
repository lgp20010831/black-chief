package com.black.core.sql.code.sqls;


import com.black.core.sql.unc.OperationType;
import com.black.core.sql.unc.SqlValue;
import com.black.table.ColumnMetadata;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SqlValueGroup {

    private final List<SqlValue> sqlValueList;

    public SqlValueGroup(){
        this(new ArrayList<>());
    }

    public SqlValueGroup(List<SqlValue> sqlValueList){
        this.sqlValueList = sqlValueList == null ? new ArrayList<>() : sqlValueList;
    }

    public SqlValueGroup addValue(SqlValue sqlValue){
        if (sqlValue != null){
            sqlValueList.add(sqlValue);
        }
        return this;
    }

    public SqlValue findSingle(String columnName){
        List<SqlValue> sqlValues = find(columnName);
        return sqlValues.isEmpty() ? null : sqlValues.get(0);
    }

    public SqlValue findSingle(String columnName, OperationType operationType){
        List<SqlValue> sqlValues = find(columnName, operationType);
        return sqlValues.isEmpty() ? null : sqlValues.get(0);
    }

    public List<SqlValue> find(String columnName){
        List<SqlValue> result = new ArrayList<>();
        for (SqlValue sqlValue : sqlValueList) {
            if (columnName.equals(sqlValue.getColumnMetadata().getName())){
                result.add(sqlValue);
            }
        }
        return result;
    }


    public List<SqlValue> find(String columnName, OperationType operationType){
        List<SqlValue> result = new ArrayList<>();
        for (SqlValue sqlValue : sqlValueList) {
            ColumnMetadata columnMetadata = sqlValue.getColumnMetadata();
            if (columnMetadata != null){
                if (columnName.equals(columnMetadata.getName()) &&
                operationType == sqlValue.getVariable().getOperationType()){
                    result.add(sqlValue);
                }
            }
        }
        return result;
    }

    public List<SqlValue> find(OperationType operationType){
        List<SqlValue> result = new ArrayList<>();
        for (SqlValue sqlValue : sqlValueList) {
            if (sqlValue.getVariable().getOperationType() == operationType){
                result.add(sqlValue);
            }
        }
        return result;
    }

    public int getCount(String columnName, OperationType operationType){
        int size = 0;
        for (SqlValue sqlValue : sqlValueList) {
            if (columnName.equals(sqlValue.getColumnMetadata().getName()) &&
                    operationType == sqlValue.getVariable().getOperationType()){
                size++;
            }
        }
        return size;
    }

    public int getCount(OperationType operationType){
        int size = 0;
        for (SqlValue sqlValue : sqlValueList) {
            if (operationType == sqlValue.getVariable().getOperationType()){
                size++;
            }
        }
        return size;
    }

}
