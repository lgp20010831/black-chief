package com.black.core.aop.servlet.item;

import java.util.Map;

public interface LAOperatorProcessor {

    char EMTRY_CHAR = 'g';

    char getStartChar();


    char getEndChar();


    Object parse(Map<String, Object> globalVariable, Object previousValue, EachParagraphOperator paragraphOperator) throws Throwable;

}
