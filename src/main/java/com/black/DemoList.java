package com.black;

import com.alibaba.fastjson.JSONObject;
import com.black.asm.Demo;
import com.black.compile.JavaDelegateCompiler;
import com.black.core.SpringAutoThymeleafApplication;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.factory.beans.AgentRequired;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.annotation.AsLazy;
import com.black.core.factory.beans.annotation.NotNull;
import com.black.core.factory.beans.annotation.ProgramTiming;
import com.black.core.factory.beans.config_collect520.Collect;
import com.black.core.factory.beans.imports.Default;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.OpenComponent;
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
        System.out.println(handler.convert(User.class, new JSONObject()));
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

        lltest();
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


    static void fa(){
        ChiefApplicationRunner.getMainClasses().add(SpringAutoThymeleafApplication.class);
        BeanFactory factory = FactoryManager.initAndGetBeanFactory();
        Cd cd = factory.getSingleBean(Cd.class);
        System.out.println(1);
        System.out.println(cd);
        System.out.println(cd.say(null));
        System.out.println(cd);
    }

    @Data
    @AgentRequired
    @AsLazy
    public static class Cd{

        public Cd(){
            System.out.println("cd执行");
        }

        static String name;

        @AsLazy
        A a;

        @Collect(scope = "com.black.xml.engine.impl")
        List<XmlNodeHandler> nodeHandlers;

        @NotNull
        @Default
        @ProgramTiming
        Object say(@Collect Map<Class<?>, OpenComponent> handlerMap){
            System.out.println(handlerMap);
            System.out.println(a);
            return null;
        }
    }

    public static class A{}


    static {
        System.out.println("赤骊骋疆，巡狩八荒！");
        System.out.println("长缨在手，百骥可降！");
        System.out.println("世皆彳亍，唯我纵横。");
        System.out.println("横枪立马，独啸秋风！");
        System.out.println("离群之马，虽强亦亡。");
        System.out.println("赶缚苍龙擒猛虎，一枪纵横定天山！");
        System.out.println("马踏祁连山河动，兵起玄黄奈何天！");
        System.out.println("一骑破霄汉，饮马星河，醉卧广寒！");
        System.out.println("雷部显圣，引赤电为翼，铸霹雳成枪！");
        System.out.println("七情难掩，六欲难消，何谓之神？");

    }


    //流量测试
    static void lltest() throws IOException {
        JavaDelegateCompiler compiler = new JavaDelegateCompiler();
        compiler.compileAndRun("$[com.black.sql_v2, com.black] Sql.opt();System.out.println($1.get(${name}));System.out.println(${hello world});", Av0.js("name", "lgp"));
    }

    interface Post{

        void fetch();


        default String param(){
            return null;
        }
    }


}
