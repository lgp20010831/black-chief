package com.black;

import com.black.core.sql.code.YmlDataSourceBuilder;
import com.black.core.util.Av0;
import com.black.datasource.MybatisPlusDynamicDataSourceBuilder;
import com.black.project.JdbcProjectGenerator;
import com.black.project.Version;
import com.black.utils.ServiceUtils;

public class DemoList {


    static void create(){
        JdbcProjectGenerator generator = new JdbcProjectGenerator(Version.MYBATIS_1_5);
        generator.setDataSourceBuilder(new MybatisPlusDynamicDataSourceBuilder());
        generator.setControllerGenPath("com.black");
        generator.setImplGenPath("com.black");
        generator.setMapperGenPath("com.black");
        generator.setPojoGenPath("com.black");
        generator.writeCode("supplier");
    }

    static void testMethod(){
        String txt = "${str.toLowerCase()} -- ${str.length()}";
        Object param = Av0.of("str", "HELLO");
        Object val = ServiceUtils.patternGetValue(param, txt);
        System.out.println(val);
    }


    public static void main(String[] args) throws Throwable{
        create();
    }
}
