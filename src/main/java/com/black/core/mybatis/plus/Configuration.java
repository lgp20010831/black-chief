package com.black.core.mybatis.plus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.black.core.aop.servlet.plus.MappingPolicy;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter @Setter
public class Configuration {

    private Map<String, String> mapping = new HashMap<>();

    private boolean in = true;

    private Map<String, ConditionValue> conditionMap = new HashMap<>();

    private Set<String> or = new HashSet<>();

    private Set<String> and = new HashSet<>();

    private BaseMapper<?> mapper;

    private Class<?> entity;

    private String groupBy;

    //map 分组, 指定一个存在的字段, 则会按照该字段值进行分组
    private String mapKey;

    private MappingPolicy policy;

    private Set<String> fieldNames = new HashSet<>();

    private Set<String> orderByAsc = new HashSet<>();

    private Set<String> orderByDesc = new HashSet<>();

}
