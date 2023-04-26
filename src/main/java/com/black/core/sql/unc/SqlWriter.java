package com.black.core.sql.unc;

import com.black.core.query.ArrayUtils;

public class SqlWriter {

    public static SqlStatement update(String tableName){
        SqlStatement statement = new UpdateStatement();
        statement.writePre("update");
        statement.writePre(tableName);
        statement.writePre("set");
        return statement;
    }


    public static SqlStatement post(String in){
        return new SqlStatement("", in);
    }

    public static SqlStatement partly(String pre){
        return new SqlStatement(pre, "");
    }

    public static SqlStatement delete(String tableName){
        SqlStatement statement = new SqlStatement();
        statement.writePre("delete");
        statement.writePre("from");
        statement.writePre(tableName);
        return statement;
    }

    public static SqlStatement insert(String tableName){
        SqlStatement statement = new InsertStatement();
        statement.writePre("insert");
        statement.writePre("into");
        statement.writePre(tableName);
        return statement;
    }

    public static SqlStatement selectCount(String tableName){
        SqlStatement statement = new SqlStatement();
        statement.writePre("select");
        statement.writePre("count(0)");
        statement.writePre("from");
        statement.writePre(tableName);
        return statement;
    }

    public static SqlStatement select(String tableName){
        return select(tableName, new String[0]);
    }


    public static SqlStatement select(String tableName, String... columnName){
        SqlStatement statement = new SqlStatement();
        statement.writePre("select");
        if (columnName != null && columnName.length > 0){
            ArrayUtils.stateFor(columnName, (s, last, first) -> {
                statement.writePre(s);
                if (!last) statement.writePre(",");
            });
        }else {
            statement.writePre("*");
        }
        statement.writePre("from");
        statement.writePre(tableName);
        return statement;
    }




}
