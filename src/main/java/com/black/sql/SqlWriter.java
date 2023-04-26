package com.black.sql;

import com.black.core.query.ArrayUtils;

public class SqlWriter {

    public static SqlOutStatement update(String tableName){
        SqlOutStatement statement = new UpdateStatement();
        statement.writePre("update");
        statement.writePre("#{tableName}");
        statement.writePre("set");
        statement.setTableName(tableName);
        return statement;
    }


    public static SqlOutStatement post(String in){
        return new SqlOutStatement("", in);
    }

    public static SqlOutStatement partly(String pre){
        return new SqlOutStatement(pre, "");
    }

    public static SqlOutStatement delete(String tableName){
        SqlOutStatement statement = new SqlOutStatement();
        statement.writePre("delete");
        statement.writePre("from");
        statement.writePre("#{tableName}");
        statement.setTableName(tableName);
        statement.setDelete(true);
        return statement;
    }

    public static SqlOutStatement insert(String tableName){
        SqlOutStatement statement = new InsertStatement();
        statement.writePre("insert");
        statement.writePre("into");
        statement.writePre("#{tableName}");
        statement.setTableName(tableName);
        return statement;
    }

    public static SqlOutStatement selectCount(String tableName){
        return selectCount(tableName, false);
    }

    public static SqlOutStatement selectCount(String tableName, boolean alias){
        SqlOutStatement statement = new SqlOutStatement();
        statement.setAsTable(alias);
        statement.writePre("select");
        statement.writePre("count(0)");
        statement.writePre("from");
        statement.writePre(alias ? "#{tableName} " + SqlOutStatement.alias : "#{tableName}");
        statement.setTableName(tableName);
        return statement;
    }

    public static SqlOutStatement select(String tableName){
        return select(tableName, new String[0]);
    }

    public static SqlOutStatement select(String tableName, boolean alias, String... columnName){
        SqlOutStatement statement = new SqlOutStatement();
        statement.setAsTable(alias);
        statement.setTableName(tableName);
        statement.writePre("select");
        if (columnName != null && columnName.length > 0){
            ArrayUtils.stateFor(columnName, (s, last, first) -> {
                statement.writePre(alias ? SqlOutStatement.alias + "." + s : s);
                if (!last) statement.writePre(",");
            });
        }else {
            statement.writePre(alias ? SqlOutStatement.alias + ".*" : "*");
        }
        statement.writePre("from");
        statement.writePre(alias ? "#{tableName} " + SqlOutStatement.alias : "#{tableName}");
        return statement;
    }


    public static SqlOutStatement select(String tableName, String... columnName){
        return select(tableName, false, columnName);
    }




}
