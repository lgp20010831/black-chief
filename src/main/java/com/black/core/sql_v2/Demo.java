package com.black.core.sql_v2;

import com.black.sql_v2.Environment;
import com.black.sql_v2.Sql;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.javassist.*;

import java.util.Map;
import java.util.Set;


//@VirtualHybrid
public class Demo {


    @Proxy(methodName = "list0", tableNames = {"*"})
    Object list(String tableName, Map<String, Object> body){
        return Sql.opt().query(tableName, body).jsonList();
    }


    //获取表名  -> 生成类  -> aop 代理  -> 注册
    //默认数据源
    @Virtual
    Object def(Set<String> names, SqlExecutor executor){
        System.out.println(executor);
        System.out.println(names);
        SqlV2ApiRemarkRegister register = SqlV2ApiRemarkRegister.getInstance();
        register.set("admin", "管理员管理");
        register.set("bank", "银行管理");
        return names;
    }

    @OptConfigurer
    void config(Environment environment){
        System.out.println(environment);
    }
}
