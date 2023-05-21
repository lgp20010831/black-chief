package com.black;

import com.alibaba.fastjson.JSONObject;
import com.black.asm.Demo;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.factory.beans.AgentRequired;
import com.black.core.factory.beans.config_collect520.Collect;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.util.Av0;
import com.black.core.util.LazyAutoWried;
import com.black.datasource.MybatisPlusDynamicDataSourceBuilder;
import com.black.ftl.FtlResolver;
import com.black.graphql.Graphqls;
import com.black.project.DEMO;
import com.black.project.JdbcProjectGenerator;
import com.black.project.ProjectEnvironmentalGuess;
import com.black.project.Version;
import com.black.sql_v2.Sql;
import com.black.sql_v2.handler.SqlStatementHandler;
import com.black.utils.IoUtils;
import com.black.utils.ServiceUtils;
import com.black.xml.XmlSql;
import com.black.xml.engine.impl.XmlNodeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DemoList {

    @LazyAutoWried
    static FtlResolver resolver;

    static void ftl(){
        TypeHandler handler = TypeConvertCache.initAndGet();
        System.out.println(handler.convert(Demo.User.class, new JSONObject()));
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
        gra();
    }


    //{"query":"{userList(user:{name:\"lgp\",age:2}){name  age}}"}
    static void gra(){
        System.out.println(Graphqls.query("http://localhost:8080/api", "userList")
                        .addObjectParam("user", new User("lgp", 2))
                .addResults("name, age")
                .getRequestMessage());
        JSONObject json = Graphqls.query("http://localhost:8080/api", "userList")
                .addResults("name, age")
                .fetch();
        System.out.println(json);
    }
    @Data @AllArgsConstructor
    static class User{
        String name;

        int age;
    }

    @Data
    @AgentRequired
    public static class Cd{

        @Collect(scope = "com.black.xml.engine.impl")
        List<XmlNodeHandler> nodeHandlers;

        void say(@Collect(scope = "com.black.sql_v2.handler") Map<Class<?>, SqlStatementHandler> handlerMap){
            System.out.println(handlerMap);
        }
    }




}
