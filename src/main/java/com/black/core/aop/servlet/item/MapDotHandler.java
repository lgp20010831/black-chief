package com.black.core.aop.servlet.item;

import com.black.core.aop.servlet.item.annotation.DotHandler;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import org.springframework.util.StringUtils;

import java.util.Map;

@DotHandler
public class MapDotHandler implements DotVariousHandler{

    @Override
    public boolean support(Object parseObject) {
        return parseObject instanceof Map;
    }

    @Override
    public Object handler(Object parseObject, Map<String, Object> globalVariable, EachParagraphOperator paragraphOperator) {
        Map<String, Object> mapSource = (Map<String, Object>) parseObject;
        String mediumQuantity = paragraphOperator.getMediumQuantity();
        if (mapSource == null){

            //几乎不可能出现的情况
            String quantity = paragraphOperator.getFrontQuantity();
            if (StringUtils.hasText(quantity)){
                Object obj = globalVariable.get(quantity);
                if (obj != null){
                    if (obj instanceof Map) {
                        mapSource = (Map<String, Object>) obj;
                    }else {
                        TypeHandler handler = TypeConvertCache.initAndGet();
                        if (handler != null){
                            mapSource = (Map<String, Object>) handler.convert(Map.class, obj);
                        }
                    }
                }
            }
        }
        if (mapSource != null){
            if (mediumQuantity.contains(DotOperatorProcessor.METHOD_INVOKE_FLAG)){
                return DotOperatorProcessor.parseMethod(parseObject, globalVariable, mediumQuantity);
            }else {
                return mapSource.get(mediumQuantity);
            }
        }
        return null;
    }
}
