package com.black.core.sql.code.condition.function;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

public abstract class BooleanFunction extends AbstractFunction {

    protected AviatorObject condition(boolean condition){
        return condition ? AviatorBoolean.TRUE : AviatorBoolean.FALSE;
    }

}
