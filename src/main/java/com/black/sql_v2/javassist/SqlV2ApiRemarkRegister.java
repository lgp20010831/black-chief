package com.black.sql_v2.javassist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SqlV2ApiRemarkRegister {

    private static SqlV2ApiRemarkRegister register;


    public synchronized static SqlV2ApiRemarkRegister getInstance() {
        if (register == null){
            register = new SqlV2ApiRemarkRegister();
        }
        return register;
    }


    private final Map<String, String> tableNameWithRemark = new ConcurrentHashMap<>();

    public void set(String tableName, String remark){
        tableNameWithRemark.put(tableName, remark);
    }

    public String get(String name){
        return tableNameWithRemark.get(name);
    }
}
