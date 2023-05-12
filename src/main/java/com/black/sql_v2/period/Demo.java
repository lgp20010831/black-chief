package com.black.sql_v2.period;

import com.alibaba.fastjson.JSONObject;
import com.black.core.util.Av0;

import java.util.Arrays;
import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-05-12 14:04
 */
@SuppressWarnings("all")
public class Demo {


    /*

        {and:{yl:12, and:{wd:500}}



     */


    static void s(){
        String sql = "select * from supplier where name = #{name}";
        List<JSONObject> list = Arrays.asList(Av0.js("name", "宁波诗兰姆汽车零部件有限公司"),
                Av0.js("name", "江苏捷迅电缆有限公司"),
                Av0.js("name", "日照新光橡胶有限公司"),
                Av0.js("name", "丹阳市光束汽车配件有限公司")
        );

        new SqlPatternProvider<>(sql, list).next((session, provider) -> {
            System.out.println("xxx");
        });
    }


    public static void main(String[] args) {

        s();
//        String sql = "SELECT\n" +
//        "valve_type,\n" +
//                "standard_no \n" +
//                "FROM\n" +
//                "valve_standard \n" +
//                "WHERE\n" +
//                "valve_category = #{name} \n" +
//                "AND ( ( dn_from IS NULL AND dn_to IS NULL ) OR age = #{age} ) \n" +
//                "AND ( ( medium IS NULL AND ?2 IS NULL ) OR ?2  IN ( SELECT regexp_split_to_table( medium, '/' ) ) ) \n" +
//                "AND ( ( class_pn IS NULL ) OR age like #{age} ) " +
//                "order by rank";

//        String s = new SqlPatternProvider<>(sql, Av0.js("name", "lgp", "age", 2)).handlerSql();
//        System.out.println(s);

        String t = "helloworld";
        //helloworld
        //w:5 6
        //hellloworld
        //w:6 7

    }

}
