package com.black.sql_v2.period;

import com.alibaba.fastjson.JSONObject;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.util.Av0;
import com.black.sql_v2.Sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-12 14:04
 */
@SuppressWarnings("all")
public class Demo {


    /*

        {and:{yl:12, and:{wd:500}}



     */
    static String getRS(){
        String sql = "with t0 as(\n" +
                "\n" +
                "SELECT max(split_part(pipe_naming_table::json ->> 'design_temp', '/', 2))::numeric max_temp, " +
                "   max(split_part(pipe_naming_table::json ->> 'design_press', '/', 2))::numeric max_press, " +
                "   workarea_no wn, group_no gn from select_condition\n" +
                "where workarea_no = ?3 and group_no = ?4\n" +
                "GROUP BY workarea_no, group_no  \n" +
                "),\n" +
                "t1 AS (\n" +
                "                 SELECT\n" +
                "                  vp.data_source,\n" +
                "                  vp.GROUP,\n" +
                "                  vp.rating,\n" +
                "                  MIN ( vp.TEMP ) AS min_temp,\n" +
                "                  MAX ( vp.press ) AS max_press \n" +
                "                 FROM\n" +
                "                  valve_pt vp\n" +
                "                 WHERE\n" +
                "                  vp.data_source = 0 \n" +
                "                  AND vp.standard_no = #{vt} \n" +
                "                  AND vp.GROUP::numeric = ?4 \n" +
                "                  AND vp.TEMP >= (select max_temp from t0)\n" +
                "                  AND vp.class_pn = ?1 \n" +
                "                 GROUP BY\n" +
                "                  vp.data_source,\n" +
                "                  vp.GROUP,\n" +
                "                  vp.rating \n" +
                "                 ),\n" +
                "                 t2 AS (\n" +
                "                 SELECT\n" +
                "                  vp.data_source,\n" +
                "                  vp.GROUP,\n" +
                "                  vp.rating,\n" +
                "                  MAX ( vp.TEMP ) AS max_temp,\n" +
                "                  MIN ( vp.press ) AS min_press \n" +
                "                 FROM\n" +
                "                  valve_pt vp\n" +
                "                 WHERE\n" +
                "                  vp.data_source = 0 \n" +
                "                  AND vp.standard_no = #{vt} \n" +
                "                  AND vp.GROUP::numeric = ?4 \n" +
                "                  AND vp.TEMP <= (select max_temp from t0) \n" +
                "                  AND vp.class_pn = ?1 \n" +
                "                 GROUP BY\n" +
                "                  vp.data_source,\n" +
                "                  vp.GROUP,\n" +
                "                  vp.rating \n" +
                "                 ),\n" +
                "                 t3 AS (\n" +
                "                 SELECT\n" +
                "                  t1.data_source,\n" +
                "                  t1.GROUP,\n" +
                "                  t1.rating,\n" +
                "                  t2.max_temp AS temp_from,\n" +
                "                  t1.min_temp AS temp_to,\n" +
                "                  t2.min_press AS press_from,\n" +
                "                  t1.max_press AS press_to \n" +
                "                 FROM\n" +
                "                  t1\n" +
                "                  INNER JOIN t2 ON t2.data_source = t1.data_source \n" +
                "                  AND t2.GROUP = t1.GROUP \n" +
                "                  AND t2.rating = t1.rating \n" +
                "                 ),\n" +
                "                 t4 AS (\n" +
                "                 SELECT\n" +
                "                  t3.data_source,\n" +
                "                  t3.GROUP,\n" +
                "                  t3.rating,\n" +
                "                 CASE\n" +
                "                   WHEN t3.temp_to = t3.temp_from THEN\n" +
                "                   t3.press_from ELSE t3.press_from + ( t3.press_to - t3.press_from ) / ( t3.temp_to - t3.temp_from ) * ( (select max_temp from t0) - t3.temp_from ) \n" +
                "                  END AS press \n" +
                "                 FROM\n" +
                "                  t3 \n" +
                "                 ) SELECT\n" +
                "                 string_agg ( ?1 || t4.rating, ',' ORDER BY t4.rating ) AS rating \n" +
                "                FROM\n" +
                "                 t4 \n" +
                "                WHERE\n" +
                "                 t4.press >= (select max_press from t0) ";
        return sql;
    }

    static String sql3 = "SELECT\n" +
            "string_agg ( face, ',' ORDER BY rank ) AS face \n" +
            "FROM\n" +
            "valve_face \n" +
            "WHERE\n" +
            "medium = #{medium}\n" +
            "OR ( class_pn = #{pn} AND #{rating} BETWEEN rating_from AND rating_to ) \n" +
            "OR ( #{dn} BETWEEN dn_from AND dn_to )";

    static void zhy(int wn, int gn){
        String sql1 = "select * from select_result_flange where workarea_no = ?1 and group_no = ?2 and nominal_dimension = #{dn} and category='FLANGE'";
        String sql2 = "select string_agg(material_type_valve, ',') list  from flange_valve where material_type_flange = #{materialType} ";

        //dn =xx
        List<Map<String, Object>> dns = new ArrayList<>();
        SqlPatternProvider.provider(sql1, dns, wn, gn)
                .filterNull()
                //获取法兰
                .next((session, provider) -> {
                    String clRating = session.getString("rating");
                    Integer rating = Integer.parseInt(clRating.substring(2));
                    session.put("rating", rating);
                    provider.putEnv("pn", "CL").putEnv("medium", "LNG");
                })
                .provider(sql2)
                .filterNull()
                //阀门材料牌号
                .next((session, provider) -> {
                    String valveType = PatternTextUtils.getArrayElement(session.getString("list"), ",", 0);
                    session.put("vt", valveType);
                })
                .provider(getRS(), "CL", wn, gn)
                //获取压力等级
                .next()
                .provider(sql3)
                //查找阀门端面
                .next();


    }

    static void s(){
        String sql = "select * from user where name = #{name}";
        List<JSONObject> list = Arrays.asList(Av0.js("name", "zs"),
                Av0.js("name", "ls"),
                Av0.js("name", "ww"),
                Av0.js("name", "hl")
        );

        String sql2 = "select * from user where id = #{id}";

        SqlPatternProvider.provider(sql, list).next((session, provider) -> {
            System.out.println("xxx");
        }).provider(sql2).next((session, provider) -> {
            System.out.println(session.map());
        });
    }

    static void t(){
        String sql = "select * from user where name = #{name}";
        List<JSONObject> list = Arrays.asList(Av0.js("name", "zs"),
                Av0.js("name", "ls"),
                Av0.js("name", "ww"),
                Av0.js("name", "hl")
        );
        String sql2 = "select * from user where id = #{id}";
        for (JSONObject object : list) {
            Map<String, Object> map = Sql.nativeQueryWithEnv(sql, object).map();
            Object id = map.get("id");
            Map<String, Object> objectMap = Sql.nativeQueryWithEnv(sql2, Av0.js("id", id)).map();
            System.out.println(objectMap);
        }
    }


    public static void main(String[] args) {

        ApplicationUtil.programRunMills(() -> {
            t();
        });
        //s();
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
