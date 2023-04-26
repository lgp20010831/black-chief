package com.black.core.sql.code.condition;

import com.black.core.util.Av0;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

public class ConditionConnect {


    public static void main(String[] args) {
        Map<String, Object> of = Av0.of("name", Av0.of("age", "ldb", "list", Av0.as(1, 2)));
        AviatorEvaluator.addFunction(new AbstractFunction() {
            @Override
            public String getName() {
                return "isAdmin";
            }

            @Override
            public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
                return "ldb".equals(arg1.getValue(env)) ? AviatorBoolean.TRUE : AviatorBoolean.FALSE;
            }
        });
        System.out.println(AviatorEvaluator.execute("(2>5 || isAdmin(name.age)) && 7>1 && list.size(name.list) > 0", of));

    }

}
