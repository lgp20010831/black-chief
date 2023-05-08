package com.black;

import com.black.bin.ApplyProxyFactory;
import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.core.sql.code.YmlDataSourceBuilder;
import com.black.core.util.Av0;

import com.black.datasource.MybatisPlusDynamicDataSourceBuilder;
import com.black.project.JdbcProjectGenerator;
import com.black.project.Version;
import com.black.sql_v2.Sql;
import com.black.utils.ServiceUtils;
import com.black.xml.XmlSql;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class DemoList {


    static void xml(){
        Sql.configDataSource(new MybatisPlusDynamicDataSourceBuilder());
        XmlSql.opt().scanAndParse("xml-sql/");
        List<Map<String, Object>> list = XmlSql.selectByArray("selectSupplier").list();
        System.out.println(list.size());
    }


    static void create(){
        JdbcProjectGenerator generator = new JdbcProjectGenerator(Version.MYBATIS_1_5);
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

    public static void main(String[] args) throws Throwable{
        xml();
    }
}
