package com.black.sql_v2.javassist;

import com.black.mvc.MvcMappingRegister;
import com.black.core.spring.ChiefApplicationHolder;
import com.black.core.spring.ChiefExpansivelyApplication;

import java.util.Collection;

public class SqlV2ProxyJavassistAnnexSpringServletHandler {

    /*
        tableName - alias

        --> controller  -> spring

        then

        SqlV2AopHybrid --> intercept controller

        ---> 实现代理
        所以该类的作用就是生成 controller 然后注册到 spring 中
     */

    public static void createAndRegister(String tableName, String alias){
        SqlV2JavassistControllerManager manager = new SqlV2JavassistControllerManager();
        createAndRegister(tableName, alias, manager);
    }

    public static void createAndRegister(String tableName, String alias, SqlV2JavassistControllerManager manager){
        Class<?> controller = manager.createController(alias, tableName);
        MvcMappingRegister.registerSupportAopController(controller);
        ChiefExpansivelyApplication expansivelyApplication = ChiefApplicationHolder.getExpansivelyApplication();
        if (expansivelyApplication != null){
            Collection<Class<?>> projectClasses = expansivelyApplication.getProjectClasses();
            projectClasses.add(controller);
        }

    }

}
