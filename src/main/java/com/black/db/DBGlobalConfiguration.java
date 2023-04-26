package com.black.db;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.util.Assert;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Setter @Getter
public class DBGlobalConfiguration {

    //java 与 数据库别名转换
    private AliasColumnConvertHandler convertHandler;

    //日志
    private IoLog log;

    //查询结果返回的类型
    private Class<? extends Map> returnType =  LinkedHashMap.class;

    //全局环境变量
    private Map<String, Object> globalEnv = new HashMap<>();

    //参数注入的时候是否用到全局环境变量
    private boolean useGlobalEnv = true;

    //全局映射 sql 语句
    private Map<String, String> globalMappingStatements = new HashMap<>();

    //是否开启 DB 的sql语句映射 %{}
    private boolean openDBGlobalMapping = true;

    //是否开启 CHIEF 的sql语句映射 ${}
    private boolean openChiefGlobalMapping = true;

    //是否开启 map 参数的注入 #{}  ^{}
    private boolean openMapParamInjection = true;

    //使用 jpa ? 参数策略   ?1, ?2
    private boolean useJpaFictitiousStrategy = true;

    //使用 ?[] 方式
    private boolean openDichotomous = true;

    //是否允许结果集上下滚动
    private boolean allowScroll = false;

    //db 封装的连接
    private DBConnection dbConnection;

    public DBConnection getDbConnection() {
        Assert.notNull(dbConnection, "dbconnection is null");
        return dbConnection;
    }

    public void init(){
        convertHandler = new HumpColumnConvertHandler();
        log = LogFactory.getLog4j();
    }
}
