package com.black;

import com.black.bin.ApplyProxyFactory;
import com.black.bin.ApplyProxyLayer;
import com.black.bin.ProxyTemplate;
import com.black.core.util.Av0;
import com.black.core.yml.pojo.Ayc;
import com.black.datasource.MybatisPlusDynamicDataSourceBuilder;
import com.black.project.JdbcProjectGenerator;
import com.black.project.Version;
import com.black.utils.ServiceUtils;

import java.lang.reflect.Method;

public class DemoList {


    static void proxy(){
        Ayc ayc = new Ayc();
        ayc = ApplyProxyFactory.proxy(ayc, new ApplyProxyLayer() {
            @Override
            public Object proxy(Object[] args, Method method, Class<?> beanClass, ProxyTemplate template) throws Throwable {
                System.out.println("执行前打印");
                return template.invokeOriginal(args);
            }
        });


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
        create();
    }
}
