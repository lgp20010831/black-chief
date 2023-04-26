package com.black.core.mybatis.plus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.black.core.aop.servlet.plus.MappingPolicy;
import com.black.core.aop.servlet.plus.Policy;
import com.black.core.tools.BeanUtil;
import com.black.utils.ReflexHandler;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigurationFactory {

    private final Configuration configuration;

    public ConfigurationFactory(){
        this(new Configuration());
    }

    public ConfigurationFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public Map<String, String> addMapping(String[] mappingStr){
        Map<String, String> mapping = configuration.getMapping();
        for (String str : mappingStr) {
            if (!str.contains("->")) {
                throw new ParseConfigurationException("指名映射关系时, 需要使用 -> 符号连接");
            }
            String[] zc = str.split("->");
            if (zc.length != 2){
                throw new ParseConfigurationException("每个条目最多指明一对映射关系");
            }
            mapping.put(zc[0].trim(), zc[1].trim());
        }
        return mapping;
    }

    public ConfigurationFactory addOr(String[] orStr){
        Set<String> or = configuration.getOr();
        or.addAll(Arrays.asList(orStr));
        return this;
    }

    public ConfigurationFactory addAnd(String[] andStr){
        Set<String> and = configuration.getAnd();
        and.addAll(Arrays.asList(andStr));
        return this;
    }

    public ConfigurationFactory addOrderDesc(String[] sc){
        Set<String> desc = configuration.getOrderByDesc();
        desc.addAll(Arrays.asList(sc));
        return this;
    }

    public ConfigurationFactory addOrderAsc(String[] sc){
        Set<String> asc = configuration.getOrderByAsc();
        asc.addAll(Arrays.asList(sc));
        return this;
    }

    public ConfigurationFactory pointMapper(BaseMapper<?> mapper){
        if (mapper == null){
            throw new ParseConfigurationException("mapper 不能为空");
        }

        try {
            Class<?>[] genericVal = ReflexHandler.genericVal(BeanUtil.getPrimordialClass(mapper), BaseMapper.class);
            if (genericVal.length != 1){
                throw new ParseConfigurationException("没有检测到 baseMapper 上泛型的存在");
            }
            Class<?> clazz = genericVal[0];
            configuration.setEntity(clazz);
            Set<String> set = ReflexHandler.getAccessibleFields(clazz)
                    .stream()
                    .map(Field::getName)
                    .collect(Collectors.toSet());
            configuration.getFieldNames().addAll(set);
            Policy policy = AnnotationUtils.getAnnotation(clazz, Policy.class);
            configuration.setPolicy(policy == null ? MappingPolicy.FieldName$column_name : policy.value());
        }catch (RuntimeException e){
            ParseConfigurationException pce;
            if (e instanceof ParseConfigurationException){
                pce = (ParseConfigurationException) e;
            }else {
                pce = new ParseConfigurationException(e);
            }
            throw pce;
        }
        configuration.setMapper(mapper);

        return this;
    }

    public Configuration open(){
        return configuration;
    }
}
