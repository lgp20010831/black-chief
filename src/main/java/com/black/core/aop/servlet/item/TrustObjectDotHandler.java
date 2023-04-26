package com.black.core.aop.servlet.item;

import com.black.core.aop.servlet.item.annotation.DotHandler;
import com.black.core.json.ReflexUtils;
import com.black.core.json.Trust;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;

@DotHandler
public class TrustObjectDotHandler implements DotVariousHandler{


    @Override
    public boolean support(Object parseObject) {
        if (parseObject != null){
            return AnnotationUtils.getAnnotation(parseObject.getClass(), Trust.class) != null;
        }
        return false;
    }

    @Override
    public Object handler(Object parseObject, Map<String, Object> globalVariable, EachParagraphOperator paragraphOperator) {
        String mediumQuantity = paragraphOperator.getMediumQuantity();
        if (mediumQuantity.endsWith(DotOperatorProcessor.METHOD_INVOKE_FLAG)){
            return DotOperatorProcessor.parseMethod(parseObject, globalVariable, mediumQuantity);
        }else {
            return ReflexUtils.getValue(ReflexUtils.getField(mediumQuantity, parseObject), parseObject);
        }
    }
}
