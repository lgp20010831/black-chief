package com.black.core.aop.servlet.plus;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableId;
import com.black.core.aop.servlet.plus.config.QueryWrapperConfiguration;
import com.black.core.builder.Col;
import lombok.Getter;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class EntryWrapper {

    //装载实体类所有字段的
    //key = alias
    private final Map<String, Field> fields = new HashMap<>();

    //实体类 class 对象
    private final Class<?> targetClazz;

    //主键
    private String primaryKey;

    //策略
    private final MappingPolicy policy;

    public static MappingPolicy DEFAULT_POLICY = MappingPolicy.FieldName$column_name;

    public static String DEFAULT_PRIMARY_KEY = "id";

    private final Set<String> orSet = new HashSet<>();

    private final Set<String> likeSet = new HashSet<>();

    //转换成 map 的数据源
    private final Map<String, Object> dynamicArgs = new ConcurrentHashMap<>();

    private final Map<String, Object> orDynamicArgs = new ConcurrentHashMap<>();

    //固定的数据源
    private final Map<String, String> finalSource = new ConcurrentHashMap<>();

    //总数据源
    private final Map<String, Object> totalSource = new ConcurrentHashMap<>();

    private final Map<String, Object> primordialArgs = new ConcurrentHashMap<>();

    //原本参数数据源
    private Object source;

    //当前参数
    private Object arg;

    public EntryWrapper(Class<?> targetClazz) {
        this.targetClazz = targetClazz;
        Policy policy = AnnotationUtils.getAnnotation(targetClazz, Policy.class);
        if (policy != null){
            this.policy = policy.value();
        }else {
            this.policy = DEFAULT_POLICY;
        }
    }


    public Set<String> getPrimordialArgNames(){
        return primordialArgs.keySet();
    }

    public void registerField(String name, Field field){
        fields.put(name, field);
    }

    public void put(String key, Object value){
        if (orSet.contains(key)){
            orDynamicArgs.put(key ,value);
        }else {
            dynamicArgs.put(key, value);
        }
    }

    public void initTotalSource(){
        totalSource.putAll(orDynamicArgs);
        totalSource.putAll(dynamicArgs);
    }

    public Object getValue(String key){
        if (dynamicArgs.containsKey(key)){
            return dynamicArgs.get(key);
        }else if (orDynamicArgs.containsKey(key)){
            return orDynamicArgs.get(key);
        }
        return null;
    }

    public Object getPrimordialValue(String key){
        return primordialArgs.get(key);
    }

    public void putAll(Map<String, Object> source){
        source.forEach(this::put);
    }

    public void groupBy(QueryWrapperConfiguration configuration){
        String[] ors = configuration.getOrConditionFields();
        if(ors != null){
            orSet.addAll(Arrays.asList(ors));
        }
        String[] likeConditionFields = configuration.getLikeConditionFields();
        if (likeConditionFields != null){
            likeSet.addAll(Col.as(likeConditionFields));
        }

        String[] conditionMap = configuration.getConditionMap();
        if (conditionMap != null){
            finalSource.putAll(MethodEntryExecutor.handlerFillSet(new HashSet<>(Col.as(conditionMap))));
        }
    }

    public void clear(){
        dynamicArgs.clear();
        orDynamicArgs.clear();
        totalSource.clear();
        primordialArgs.clear();
        source = null;
        arg = null;
    }

    public String getPrimaryKey(){
        if (primaryKey == null){
            fields.forEach((n, f) ->{
                if (AnnotationUtils.getAnnotation(f, TableId.class) != null){
                    primaryKey = n;
                }
            });
            if (primaryKey == null){
                primaryKey = DEFAULT_PRIMARY_KEY;
            }
        }
        return primaryKey;
    }

    public void setSource(Object source, JSONObject json) {
        this.source = source;
        primordialArgs.putAll(json);
    }

    public void reset(Object arg){
        this.arg = arg;
    }
}
