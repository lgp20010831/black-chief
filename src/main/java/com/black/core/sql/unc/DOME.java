package com.black.core.sql.unc;


public class DOME {


    public static void main(String[] args) {
//        SqlStatement statement = SqlWriter.update("supplier")
//                .writeSet("name", "sss")
//                .writeSetVariable("code", "?")
//                .writeEq("id", "a123")
//                .writeEq("id", "?", false)
//                .finish();
//        String sql = statement.toString();
//        System.out.println(sql);
        SqlStatement statement = SqlWriter.select("ayc")
                .openRetain()
                .writeEq("id", "?", false)
                .writeEq("qwqwq", "?", false)
                .writeAnd(i -> {
                    i.writeEq("name", "?", false)
                            .filp()
                            .writeEq("code", "?", false)
                            .writeNotEq("ty", "?", false);
                })
                //.replaceOperation("id", "uuid()", true)
                //.removeOperation("name")
                .writeEq("ui", "23", false)
                .writeEq("oi", "?", false)
                .resetTableName("byc")
                .finish();
        System.out.println(statement.group());
        System.out.println(statement);
        System.out.println(SqlWriter.insert("yu")
                .resetTableName("iux"));
        System.out.println(SqlWriter.update("ooo")
                .resetTableName("pppp"));
    }
}
