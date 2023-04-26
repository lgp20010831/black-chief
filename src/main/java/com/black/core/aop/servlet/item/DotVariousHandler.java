package com.black.core.aop.servlet.item;

import java.util.Map;

public interface DotVariousHandler {

    boolean support(Object parseObject);

    Object handler(Object parseObject, Map<String, Object> globalVariable, EachParagraphOperator paragraphOperator);
}
