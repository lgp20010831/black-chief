package com.black.core.aop.servlet.plus;

import com.baomidou.mybatisplus.annotation.TableId;
import com.black.utils.ReflexHandler;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractWrapperParser implements WrapperParser {

    protected final Map<Class<?>, String> primaryKeyMap = new HashMap<>();

    public static final String IN_OPERATOR = "in";

    public static final String NOT_IN_OPERATOR = "not in";

    public static String DEFAULT_KEY = "id";

    protected final MappingPolicyHandler policyHandler;

    private final Map<Class<?>, MappingPolicy> policyMap = new HashMap<>();

    private final MappingPolicy defaultPolocy = MappingPolicy.FieldName$column_name;

    protected AbstractWrapperParser(MappingPolicyHandler policyHandler) {
        this.policyHandler = policyHandler;
    }


    protected MappingPolicy getPolicy(Class<?> beanClass){
        MappingPolicy mappingPolicy = policyMap.get(beanClass);
        if (mappingPolicy == null){
            Policy policy = AnnotationUtils.getAnnotation(beanClass, Policy.class);
            mappingPolicy = policy == null ? defaultPolocy : policy.value();
            policyMap.put(beanClass, mappingPolicy);
        }
        return mappingPolicy;
    }

    protected MappingPolicy getPolicy(Object bean){
        return getPolicy(bean.getClass());
    }

    protected String getKey(Class<?> entity){
        String key = primaryKeyMap.get(entity);
        if (key == null){
            for (Field field : ReflexHandler.getAccessibleFields(entity)) {
                TableId tableId = AnnotationUtils.getAnnotation(field, TableId.class);
                if (tableId != null){
                    key = field.getName();
                    break;
                }
            }
            if (key == null){
                key = DEFAULT_KEY;
            }
            primaryKeyMap.put(entity, key);
        }
        return key;
    }

}
