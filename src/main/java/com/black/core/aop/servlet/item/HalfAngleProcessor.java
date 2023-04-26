package com.black.core.aop.servlet.item;

import com.black.core.aop.servlet.item.annotation.OperatorProcessor;

import java.util.Map;

@OperatorProcessor
public class HalfAngleProcessor extends AbstractLAOperatorProcessor{

    public HalfAngleProcessor() {
        super('^');
    }

    @Override
    public Object parse(Map<String, Object> globalVariable, Object previousValue, EachParagraphOperator paragraphOperator) throws Throwable {
        String mediumQuantity = paragraphOperator.getMediumQuantity().trim();
        if (previousValue == null){
            String frontQuantity = paragraphOperator.getFrontQuantity().trim();
            previousValue = globalVariable.get(frontQuantity);
        }

        if (previousValue != null){
            globalVariable.put(mediumQuantity, previousValue);
        }
        return previousValue;
    }
}
