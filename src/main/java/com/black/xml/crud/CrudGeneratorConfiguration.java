package com.black.xml.crud;

import com.black.core.sql.unc.OperationType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 李桂鹏
 * @create 2023-06-02 13:47
 */
@SuppressWarnings("all") @Getter @Setter
public class CrudGeneratorConfiguration {

    //使用的表名
    private String tableName;

    //使用简单的模板创建
    private boolean useSimple = true;

    private Set<OperationType> allowOperationType = new LinkedHashSet<>();

    private Set<String> hiddleMethods = new HashSet<>();

    public CrudGeneratorConfiguration(){
        allowOperationType.add(OperationType.INSERT);
        allowOperationType.add(OperationType.DELETE);
        allowOperationType.add(OperationType.UPDATE);
        allowOperationType.add(OperationType.SELECT);
    }

    public boolean isHiddle(String methodName){
        return hiddleMethods.contains(methodName);
    }

    public boolean isAllow(OperationType type){
        return allowOperationType.contains(type);
    }


}
