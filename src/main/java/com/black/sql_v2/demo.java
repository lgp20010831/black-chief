package com.black.sql_v2;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.api.JSONTool;
import com.black.datasource.MybatisPlusDynamicDataSourceBuilder;
import com.black.nest.NestManager;
import com.black.role.UserLocal;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.code.cascade.Strategy;
import com.black.core.util.Av0;
import com.black.sql_v2.print.IoLogResultPrinter;
import com.black.sql_v2.result.SubsetSearchResultHandler;
import com.black.sql_v2.with.WithAs;

import java.util.List;

import static com.black.utils.ServiceUtils.ofMap;

public class demo {


    static void prepare(){
        GlobalEnvironment environment = GlobalEnvironment.getInstance();
        environment.setDataSourceBuilder(new MybatisPlusDynamicDataSourceBuilder());
        environment.setInsertBatch(1000);
        environment.parseAndRegister("where[is_deleted = false], set[updated_at = now()], insert[is_deleted = false, inserted_at=now()]");
        environment.registerKeyAndValue(SqlType.INSERT, "create_user", UserLocal.getName());
        SubsetSearchResultHandler.strategy = Strategy.GROUP_BY;

        Environment optEnvironment = Sql.optEnvironment();
    }

    static void withas(){
        Sql.query("supplier",
                WithAs.of("supplier_type", "st"), "$A: st.id = r.supplier_type_id");
    }

    static void nest(){
        NestManager.init(MybatisPlusDynamicDataSourceBuilder.class);
    }

    static void query(){
        List<JSONObject> jsonObjects = Sql.query("supplier",
                "JSON: {name : 山}", "$B: like[name]", "open dict", "nest v2", "set statement: fetchSize = 20").jsonList();
        System.out.println(jsonObjects);
    }

    static void query2(){
        Object list = Sql.query("songsheet", new IoLogResultPrinter(), "JSON: {cauthor: 华晨宇, src: c}", "$B: like[src]", "nest v2").jsonList();
        //System.out.println(list);
        System.out.println(JSONTool.formatJson(new JSONArray((List<Object>) list).toJSONString()));
    }

    static void insert(){
        JSONArray array = new JSONArray();
        for (int i = 0; i < 20000; i++) {
            array.add(Av0.js("src", "麻豆tv", "filename", "xx00"));
        }
        System.out.println(Sql.insertBatch("img", array));
    }

    static void delete(){
        Sql.delete("img", ofMap("src", "麻豆tv"));
    }

    static void update(){
        Sql.update("img", ofMap("uploadname", "李sira"), ofMap("id", 179));
    }

    public static void main(String[] args) {
        ApplicationUtil.programRunMills(() ->{
            prepare();
            query();
        });

        //Sql.opt().save(new Ayc());
        //nest();
        //ThreadUtils.runThreads(demo::query, 10);


    }

}
