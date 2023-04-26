package com.black.core.builder;

import com.black.core.query.ArrayUtils;
import com.black.core.sql.code.SqlHelper;
import com.black.core.util.Assert;
import lombok.AllArgsConstructor;

import java.util.*;

public class SqlBuilder {

    public static SqlStatement update(String tableName){
        SqlStatement statement = new SqlStatement();
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
        SqlStatement statement = new SqlStatement();
        statement.writePre("insert");
        statement.writePre("into");
        statement.writePre(tableName);
        return statement;
    }

    public static SqlStatement select(String tableName){
        return select(tableName, new String[0]);
    }

    public static SqlStatement select(String tableName, String... cs){
        SqlStatement statement = new SqlStatement();
        statement.writePre("select");
        if (cs != null && cs.length > 0){
            ArrayUtils.stateFor(cs, (s, last, first) -> {
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

    @AllArgsConstructor
    public static class SqlValueAndType{
        String value;
        boolean strType;
    }

    public static class SqlStatement{
        //不包含 where 之后的语句

        final StringBuilder pre;

        final StringBuilder in;

        boolean hasWhere = false;

        boolean pointInsertValues = false;

        boolean hasValues = false;

        List<String> insertSort;

        List<Map<String, SqlValueAndType>> values;

        public SqlStatement(){
            this("", "");
        }

        public SqlStatement(String pre, String in) {
            this.in = new StringBuilder(in);
            this.pre = new StringBuilder(pre);
        }

        public boolean isHasWhere() {
            return hasWhere;
        }

        public String createSql(){
            return checkPre() + checkIn();
        }

        public SqlStatement replaceSet(String column, String seq, boolean type){
            int i = pre.indexOf(column);
            if (i == -1){
                writeSet(column, seq, type);
                return this;
            }
//            pre.replace(column, )
            return this;
        }


        private String removeTrailingSpace(String sql){
            while (sql.endsWith(" ")){
                sql = sql.substring(0, sql.length() - 1);
            }
            return sql;
        }


        public SqlStatement writeInsertColumnArray(String... cs){
            if (insertSort == null) insertSort = new ArrayList<>();
            insertSort.addAll(Arrays.asList(cs));
            return this;
        }

        public SqlStatement writeInsertValue(String column, String value){
            return writeInsertValue(column, value, true);
        }

        public SqlStatement writeInsertValue(String column, String value, boolean type){
            if (values == null) values = new ArrayList<>();
            if (insertSort == null) throw new IllegalStateException("拼接添加sql需要先指定插入顺序");
            boolean join = false;
            for (Map<String, SqlValueAndType> map : values) {
                if (map.containsKey(column)) {
                    continue;
                }
                map.put(column, new SqlValueAndType(value, type));
                join = true;
                break;
            }
            if (!join){
                Map<String, SqlValueAndType> map = new HashMap<>();
                map.put(column, new SqlValueAndType(value, type));
                values.add(map);
            }
            return this;
        }


        public SqlStatement writeInsertColumns(){
            writePre("(");
            ArrayUtils.stateFor(insertSort, (s, last, first) -> {
                writePre(s);
                if (!last) writePre(",");
            });
            writePre(")");
            return this;
        }

        private String checkIn(){

            String inStr = removeTrailingSpace(in.toString());
            for (;;){
                if (inStr.endsWith("and")){
                    inStr = inStr.substring(0, inStr.lastIndexOf("and"));
                    inStr = removeTrailingSpace(inStr);
                }else break;
            }

            for (;;){
                if (inStr.endsWith("or")){
                    inStr = inStr.substring(0, inStr.lastIndexOf("or"));
                    inStr = removeTrailingSpace(inStr);
                }else break;
            }
            return inStr;
        }

        private String checkPre(){
            if (values != null){
                Assert.notNull(insertSort, "sort is null");
                writeInsertColumns();
                writePre("values");
                ArrayUtils.stateFor(values, (stringSqlValueAndTypeMap, last, first) -> {
                    writePre("(");
                    ArrayUtils.stateFor(insertSort, (s, la, fir) -> {
                        SqlValueAndType type = stringSqlValueAndTypeMap.get(s);
                        if (type == null){
                            writePre("null");
                        }else {
                            writePre(SqlHelper.getString(type.value, type.strType));
                        }
                        if (!la) writePre(",");
                    });
                    writePre(")");
                    if (!last) writePre(",");
                });
            }
            String preStr = removeTrailingSpace(pre.toString());
            for (;;){
                if (preStr.endsWith(",")){
                    preStr = preStr.substring(0, preStr.length() - 1);
                    preStr = removeTrailingSpace(preStr);
                }else break;
            }
            return preStr;
        }

        public SqlStatement writeSet(String column, String value){
            return writeSet(column, value, true);
        }

        public SqlStatement writeSet(String column, String value, boolean type){
            writePre(column);
            writePre("=");
            writePre(SqlHelper.getString(value, type));
            writePre(",");
            return this;
        }

        public SqlStatement writeWhere(){
            in.append(" where ");
            hasWhere = true;
            return this;
        }

        public void writePre(String sql){
            if (!sql.startsWith(" ")){
                pre.append(" ");
            }
            pre.append(sql);
        }

        public SqlStatement write(String sql){
            if (!sql.startsWith(" ")){
                in.append(" ");
            }
            in.append(sql);
            return this;
        }

        public SqlStatement writeNotIn(String column, String... value){
            return writeNotIn(column, true, value);
        }

        public SqlStatement writeNotIn(String column, boolean type, String... value){
            write(column);
            write("not in");
            write("(");
            ArrayUtils.stateFor(value, (s, last, first) -> {
                write(SqlHelper.getString(s, type));
                if (!last)
                    write(",");
            });
            write(")");
            return this;
        }

        public SqlStatement writeIn(String column, String... value){
            return writeIn(column, true, value);
        }

        public SqlStatement writeIn(String column, boolean type, String... value){
            write(column);
            write("in");
            write("(");
            ArrayUtils.stateFor(value, (s, last, first) -> {
                write(SqlHelper.getString(s, type));
                if (!last)
                    write(",");
            });
            write(")");
            return this;
        }

        public SqlStatement writeOr(){
            write("or");
            return this;
        }

        public SqlStatement writeAnd(){
            write("and");
            return this;
        }

        public SqlStatement writeOrMap(Map<String, String> kvmap, boolean type){
            write("(");
            kvmap.forEach((k, v) ->{
                writeOr(k, v, type);
            });
            write(")");
            return this;
        }


        public SqlStatement writeEqMap(Map<String, String> kvmap, boolean type){
            write("(");
            kvmap.forEach((k, v) ->{
                writeAnd(k, v, type);
            });
            write(")");
            return this;
        }

        public SqlStatement writeIsNull(String column){
            if (!hasWhere){
                writeWhere();
            }
            write(column);
            write("is null");
            return this;
        }

        public SqlStatement writeIsNotNull(String column){
            if (!hasWhere){
                writeWhere();
            }
            write(column);
            write("is not null");
            return this;
        }

        public SqlStatement writeAnd(String column, String value){
            return writeAnd(column, value, true);
        }

        public SqlStatement writeAnd(String column, String value, boolean type){
            if (!hasWhere){
                writeWhere();
            }else writeAnd();
            writeEq(column, value, type);
            return this;
        }

        public SqlStatement writeOr(String column, String value){
            return writeOr(column, value, true);
        }

        public SqlStatement writeOr(String column, String value, boolean type){
            if (!hasWhere){
                writeWhere();
            }else writeOr();
            writeEq(column, value, type);
            return this;
        }

        public SqlStatement writeEq(String column, String value){
            return writeEq(column, value, true);
        }

        public SqlStatement writeEq(String column, String value, boolean type){
            if (!hasWhere){
                writeWhere();
            }
            write(column);
            write("=");
            write(SqlHelper.getString(value, type));
            return this;
        }
    }
}
