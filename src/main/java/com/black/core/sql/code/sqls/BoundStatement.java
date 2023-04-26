package com.black.core.sql.code.sqls;

import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter @AllArgsConstructor
public class BoundStatement {

    private SqlOutStatement statement;

    //每一个字段 --->  每一种操作类型  都可能有多个值存在
    private final Map<String, Map<OperationType, List<MappingVal>>> mappingValListMap = new ConcurrentHashMap<>();

    public Collection<MappingVal> getMappingVals(){
        List<MappingVal> result  = new ArrayList<>();
        mappingValListMap.forEach((cn, m) ->{
            for (List<MappingVal> vals : m.values()) {
                result.addAll(vals);
            }
        });
        return result;
    }

    public void addMV(MappingVal mappingVal){
        if (mappingVal != null){

            Map<OperationType, List<MappingVal>> valMap = mappingValListMap.computeIfAbsent(mappingVal.getColumnName(), n -> new ConcurrentHashMap<>());
            OperationType type = mappingVal.getOperationType();
            List<MappingVal> vals = valMap.computeIfAbsent(type, t -> new ArrayList<>());
            vals.add(mappingVal);
        }
    }

    public MappingVal getSingleMappingVal(String columnName, OperationType operationType){
        List<MappingVal> mappingVals = getMappingVal(columnName, operationType);
        return mappingVals == null ? null : mappingVals.get(0);
    }

    public List<MappingVal> getMappingVal(String columnName, OperationType operationType){
        Map<OperationType, List<MappingVal>> map = mappingValListMap.get(columnName);
        if (map != null){
            return map.get(operationType);
        }
        return null;
    }

    public Collection<MappingVal> getMappingVal(String columnName){
        Map<OperationType, List<MappingVal>> map = mappingValListMap.get(columnName);
        List<MappingVal> vals = new ArrayList<>();
        if (map != null){
            for (List<MappingVal> value : map.values()) {
                vals.addAll(value);
            }
        }
        return vals;
    }


    public List<MappingVal> getMappingVal(OperationType operationType){
        List<MappingVal> vals = new ArrayList<>();
        mappingValListMap.forEach((cn, m) ->{
            for (OperationType type : m.keySet()) {
                if (type == operationType){
                    vals.addAll(m.get(type));
                }
            }
        });
        return vals;
    }
}
