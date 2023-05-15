package com.black;

import com.alibaba.fastjson.JSONObject;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.util.Av0;
import com.black.core.util.LazyAutoWried;
import com.black.datasource.MybatisPlusDynamicDataSourceBuilder;
import com.black.ftl.FtlResolver;
import com.black.project.DEMO;
import com.black.project.JdbcProjectGenerator;
import com.black.project.ProjectEnvironmentalGuess;
import com.black.project.Version;
import com.black.sql_v2.Sql;
import com.black.utils.IoUtils;
import com.black.utils.ServiceUtils;
import com.black.xml.XmlSql;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DemoList {

    @LazyAutoWried
    static FtlResolver resolver;

    static void ftl(){

    }

    static void xml(){
        Sql.configDataSource(new MybatisPlusDynamicDataSourceBuilder());
        XmlSql.opt().scanAndParse("xml-sql/");
        JSONObject map = Av0.js("age", 3,
                "state", 1,
                "list", Arrays.asList("山", "东", "济南"),
                "map", Av0.of("phone", "123")
        );
        ApplicationUtil.programRunMills(() -> {
            List<Map<String, Object>> list = XmlSql.select("countSupplier", map).list();
            System.out.println(list.size());
        });

    }


    static void create(){
        DEMO.onlyCreatePojo = true;
        JdbcProjectGenerator generator = new JdbcProjectGenerator(Version.SQL_V4);
        ProjectEnvironmentalGuess guess = new ProjectEnvironmentalGuess();
        guess.setInSwagger(false);
        guess.setSimpleController(false);
        generator.addSources(guess);
        generator.setDataSourceBuilder(new MybatisPlusDynamicDataSourceBuilder());
        generator.setPathPrefix("com.black.core.yml");
        generator.setControllerGenPath("controller");
        generator.setImplGenPath("impl");
        generator.setMapperGenPath("mapper");
        generator.setPojoGenPath("pojo");
        generator.writeCode("ayc");
    }

    static void testMethod(){
        String txt = "${str.toLowerCase()} -- ${str.length()}";
        Object param = Av0.of("str", "HELLO");
        Object val = ServiceUtils.patternGetValue(param, txt);
        System.out.println(val);
    }

    static void java(){
        Java java = new Java();
        System.out.println(java);
    }

    static void read() throws IOException {
        InputStream inputStream = ServiceUtils.getNonNullResource("static/info.0.log");
        byte[] buf = IoUtils.readBytes(inputStream);
        System.out.println(buf);
    }

    public static void main(String[] args) throws Throwable{
        create();

    }
}
