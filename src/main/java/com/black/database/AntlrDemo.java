package com.black.database;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Av0;
import com.black.database.calcite.*;
import com.black.sql_v2.Sql;
import com.black.sql_v2.SqlExecutor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-06-26 10:48
 */
@SuppressWarnings("all")
public class AntlrDemo {

    public static void main(String[] args) throws SQLException {
        ApplicationUtil.programRunMills(() -> {
            //testc();
            testm();
        });

    }

    static void query() throws SQLException {
        String usql = "update user_info set id = '16' where name = 'ls'";
        String sql = "select DISTINCT * from user_info where name = 'zs' or id > 10";
        BasicMemoryTable table = new BasicMemoryTable("user_info");
        table.addColumns(new BasicMemoryColumn("id", String.class), new BasicMemoryColumn("name", String.class));
        table.addData(
                Av0.of("id", "2", "name", "zs"),
                Av0.of("id", "4", "name", "ls"),
                Av0.of("id", "34", "name", "lgp")
        );
        MemorySchema schema = MemorySchema.create("demo", table);
        SchemaManager.registerSchema(schema);
        SchemaDataSource dataSource = new SchemaDataSource("demo");
        Connection connection = dataSource.getConnection();
        SQLUtils.executeSql(usql, connection);
        List<Map<String, Object>> maps = SQLUtils.runJavaSelect(sql, connection, null);
        System.out.println(maps);
    }


    static void testm(){
        SqlExecutor executor = Sql.opt();
        executor.getEnvironment().setOpenMemorySelect(true);
        CalciteUtils.pullCall(() -> {
            for (int i = 0; i < 200; i++) {
                System.out.println(Sql.query("user_info", Av0.js("age", i), "$B: >[age]").list());
            }
            return null;
        }, executor, null, "user_info");
    }


    static void testc(){
        for (int i = 0; i < 200; i++) {
            System.out.println(Sql.query("user_info", Av0.js("age", i), "$B: >[age]").list());
        }
    }

}
