package com.black.rpc.demo;

import com.black.rpc.annotation.Actuator;
import com.black.rpc.annotation.Input;
import com.black.core.sql.code.MapperRegister;
import com.black.core.sql.code.log.SystemLog;
import com.black.core.sql.code.dome.ParentMapper;
import com.black.core.sql.code.pattern.ConversionTableNameListener;
import com.black.sql.NativeQueryManager;

import java.util.List;

public class Action {

    ParentMapper parentMapper;

    public Action(){
        MapperRegister register = MapperRegister.getInstance();
        parentMapper = register.getMapper(ParentMapper.class);
        register.registerListener(new ConversionTableNameListener());
    }

    @Actuator
    Object select(@Input String sql, @Input(required = false) List<Object> params){
        System.out.println("执行器触发, sql:" + sql);
        System.out.println("执行器触发, params:" + params);
        return NativeQueryManager.createQuery(sql, "master", params == null ? null : params.toArray())
                .setLog(new SystemLog())
                .execute().jsonList();
    }

}
