package com.black.premission.mybatis_plus;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.black.ibtais.BlendWrapperProcessor;
import com.black.premission.GlobalRUPConfiguration;
import com.black.premission.GlobalRUPConfigurationHolder;
import com.black.core.query.ClassWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.parse.BlendObject;
import com.black.core.sql.code.parse.CharParser;
import com.black.core.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RUPWrapperResolver {

    protected static final BlendWrapperProcessor blendWrapperProcessor = new BlendWrapperProcessor();

    protected static final Map<String, List<BlendObject>> cache = new ConcurrentHashMap<>();


    public static <T> UpdateWrapper<T> wriedUpdateWrapper(Class<T> type, Map<String, Object> map){
        if (map == null){
            return new UpdateWrapper<>();
        }
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        ClassWrapper<T> cw = ClassWrapper.get(type);
        Set<String> names = cw.getFieldNames();
        AliasColumnConvertHandler convertHandler = configuration.getConvertHandler();
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();
        for (String alias : map.keySet()) {
            if (names.contains(alias)){
                String column = convertHandler.convertColumn(alias);
                Object val = map.get(alias);
                updateWrapper.set(column, val);
            }
        }
        return updateWrapper;
    }

    public static <T> AbstractWrapper<?, String, ?> wriedWrapper(AbstractWrapper<?, String, ?> wrapper, Class<T> type, Map<String, Object> body){
        if (body == null){
            return new QueryWrapper<>();
        }
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        String blendString = configuration.getBlendString();
        if (StringUtils.hasText(blendString)){
            return wriedBlendWrapper(wrapper, type, body, blendString);
        }else {
            return wriedEQWrapper(wrapper, type, body);
        }
    }

    public static <T> AbstractWrapper<?, String, ?> wriedBlendWrapper(AbstractWrapper<?, String, ?> wrapper, Class<T> type, Map<String, Object> body, String blendString){
        ClassWrapper<T> cw = ClassWrapper.get(type);
        Set<String> names = cw.getFieldNames();
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        AliasColumnConvertHandler convertHandler = configuration.getConvertHandler();
        List<BlendObject> objects = cache.computeIfAbsent(blendString, CharParser::parseBlend);
        for (BlendObject object : objects) {
            String operator = object.getName();
            //要操作的列名
            List<String> attributes = object.getAttributes();
            for (String attribute : attributes) {
                boolean or = attribute.startsWith("!");
                if (or){
                    attribute = StringUtils.removeIfStartWith(attribute, "!");
                }
                if (body.containsKey(attribute) && names.contains(attribute)){
                    Object val = body.get(attribute);
                    String column = convertHandler.convertColumn(attribute);
                    blendWrapperProcessor.processor(operator, column, val, or, wrapper);
                }
            }
        }
        return wrapper;
    }

    public static <T> AbstractWrapper<?, String, ?> wriedEQWrapper(AbstractWrapper<?, String, ?> wrapper, Class<T> type, Map<String, Object> body){
        ClassWrapper<T> cw = ClassWrapper.get(type);
        Set<String> names = cw.getFieldNames();
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        AliasColumnConvertHandler convertHandler = configuration.getConvertHandler();
        for (String alias : body.keySet()) {
            if (names.contains(alias)){
                Object value = body.get(alias);
                wrapper.eq(convertHandler.convertColumn(alias), value);
            }
        }
        return wrapper;
    }
}
