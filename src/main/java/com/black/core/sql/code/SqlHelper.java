package com.black.core.sql.code;



import com.black.core.builder.SqlBuilder;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;

import java.util.*;

public class SqlHelper {

    //name ==> 'name'
    public static String getString(String str, boolean stringType){
        return stringType ? "'" + str + "'" : str;
    }

    public static List<String> getDefaultSqlSeq(int size){
        List<String> seqList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            seqList.add("?");
        }
        return seqList;
    }

    public static String getColumnNamesSql(List<String> names){
        StringBuilder builder = new StringBuilder("(");
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            builder.append(name);
            if (i != names.size() - 1){
                builder.append(",");
            }
        }
        return builder.append(")").toString();
    }

    public static String getQuestionMarkStationCharacter(List<String> sqlSequences){
        StringBuilder builder = new StringBuilder("(");
        for (int i = 0; i < sqlSequences.size(); i++) {
            builder.append(sqlSequences.get(i));
            if (i != sqlSequences.size() - 1){
                builder.append(", ");
            }
        }
        return builder.append(")").toString();
    }

    public static String getValuesSuffix(List<String> sqlSequences, int howManySource){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < howManySource; i++) {
            builder.append(getQuestionMarkStationCharacter(sqlSequences));
            if (i != howManySource - 1){
                builder.append(",");
            }
        }
        return builder.toString();
    }


    public static String createDeletePrefix(String tableName){
        return StringUtils.linkStr("delete from ", tableName);
    }

    public static String createSelectPrefix(String tableName, Set<String> columnSet){
        StringBuilder builder = new StringBuilder();
        for (String cs : columnSet) {
            builder.append(cs);
            builder.append(",");
        }
        String columnSql = builder.toString();
        columnSql = StringUtils.removeIfEndWith(columnSql, ",");
        return StringUtils.linkStr("select ", columnSql, " from ", tableName);
    }

    public static String writeKV(String k, String v){
        return "," + k + " = " + v;
    }


    public static String buildDeleteSql(String tn, Map<String, String> wherevalues, String applySql){
        SqlBuilder.SqlStatement statement = SqlBuilder.delete(tn);
        if (wherevalues != null && !wherevalues.isEmpty()){
            for (String k : wherevalues.keySet()) {
                statement.writeAnd(k,  wherevalues.get(k), false);
            }
        }
        if (StringUtils.hasText(applySql)){
            if (applySql.startsWith("and") && !statement.isHasWhere()){
                statement.writeWhere();
                applySql = applySql.substring(3);
            }
            statement.write(applySql);
        }
        return statement.createSql();
    }


    public static String buildUpdateSql(String tn, Map<String, String> setvalues, Map<String, String> wherevalues, String applySql){
        if (setvalues.size() == 0) throw new IllegalArgumentException("set values must not is null");
        SqlBuilder.SqlStatement statement = SqlBuilder.update(tn);
        for (String k : setvalues.keySet()) {
            statement.writeSet(k, setvalues.get(k), false);
        }

        if (wherevalues != null && !wherevalues.isEmpty()){
            for (String k : wherevalues.keySet()) {
                statement.writeAnd(k, wherevalues.get(k), false);
            }
        }

        if (StringUtils.hasText(applySql)){
            if (applySql.startsWith("and") && !statement.isHasWhere()){
                statement.writeWhere();
                applySql = applySql.substring(3);
            }
            statement.write(applySql);
        }
        return statement.createSql();
    }

    public static Set<String> getSetSet(String  ws){
        String[] ss = ws.split(",");
        Set<String> set = new HashSet<>();
        for (String s : ss) {
            String[] split = s.split("=");
            if (split.length == 2){
                set.add(split[0].trim());
            }
        }
        return set;
    }

    public static String replace(String sqlSet){
        Assert.notNull(sqlSet, "set sql 不能为空");
        StringBuilder builder = new StringBuilder();
        for (String se : sqlSet.split(",")) {
            String[] sa = se.split("=");
            if (sa.length == 2){
                builder.append(sa[0]);
                builder.append("=");
                builder.append("?");
                builder.append(",");
            }
        }
        String columnSql = builder.toString();
        if (columnSql.endsWith(",")){
            columnSql = columnSql.substring(0, columnSql.length() - 1);
        }
        return columnSql;
    }
}
