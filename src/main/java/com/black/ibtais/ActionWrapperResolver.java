package com.black.ibtais;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.parse.BlendObject;
import com.black.core.sql.code.parse.CharParser;
import com.black.core.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActionWrapperResolver<T> {

    protected static final BlendWrapperProcessor blendWrapperProcessor = new BlendWrapperProcessor();

    protected final Map<String, List<BlendObject>> cache = new ConcurrentHashMap<>();

    private final BusinessTransformation controller;

    public ActionWrapperResolver(BusinessTransformation controller) {
        this.controller = controller;
    }

    public UpdateWrapper<T> wiredUpdateWrapper(UpdateWrapper<T> wrapper, Class<T> type, Map<String, Object> map){
        if (map == null){
            return wrapper;
        }
        ClassWrapper<T> cw = ClassWrapper.get(type);
        Set<String> names = cw.getFieldNames();
        AliasColumnConvertHandler convertHandler = controller.getConvertHandler();
        for (String alias : map.keySet()) {
            if (names.contains(alias)){
                String column = convertHandler.convertColumn(alias);
                Object val = map.get(alias);
                wrapper.set(column, val);
            }
        }
        return wrapper;
    }

    public UpdateWrapper<T> wiredUpdateWrapper(Class<T> type, Map<String, Object> map){
        return wiredUpdateWrapper(new UpdateWrapper<>(), type, map);
    }

    public AbstractWrapper<T, String, ?> wriedWrapper(AbstractWrapper<T, String, ?> wrapper, Class<T> type, Map<String, Object> body){
        if (body == null){
            return new QueryWrapper<>();
        }
        String blendString = controller.getBlendString();
        if (StringUtils.hasText(blendString)){
            return wriedBlendWrapper(wrapper, type, body, blendString);
        }else {
            return wriedEQWrapper(wrapper, type, body);
        }
    }

    public AbstractWrapper<T, String, ?> wriedBlendWrapper(AbstractWrapper<T, String, ?> wrapper, Class<T> type, Map<String, Object> body, String blendString){
        ClassWrapper<T> cw = ClassWrapper.get(type);
        Set<String> names = cw.getFieldNames();
        AliasColumnConvertHandler convertHandler = controller.getConvertHandler();
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

    public AbstractWrapper<T, String, ?> wriedEQWrapper(AbstractWrapper<T, String, ?> wrapper, Class<T> type, Map<String, Object> body){
        ClassWrapper<T> cw = ClassWrapper.get(type);
        Set<String> names = cw.getFieldNames();
        AliasColumnConvertHandler convertHandler = controller.getConvertHandler();
        for (String alias : body.keySet()) {
            if (names.contains(alias)){
                Object value = body.get(alias);
                wrapper.eq(convertHandler.convertColumn(alias), value);
            }
        }
        return wrapper;
    }
}
