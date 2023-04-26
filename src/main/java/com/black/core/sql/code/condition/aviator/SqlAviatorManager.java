package com.black.core.sql.code.condition.aviator;

import com.black.aviator.AviatorManager;
import com.black.core.sql.code.condition.function.IsArrayFunction;
import com.black.core.sql.code.condition.function.IsMapFunction;
import com.black.core.sql.code.condition.function.NotNullFunction;

import java.util.Map;

public class SqlAviatorManager {



    private static boolean init = false;

    public static boolean isOpen(){
        return AviatorManager.isEffective();
    }

    public static boolean execute(String expression, Map<String, Object> source){
        Object result = AviatorManager.getInstance().execute(expression, source);
        if (result instanceof Boolean){
            return (boolean) result;
        }
        throw new IllegalStateException("表达式预期的结果应该是布尔值, 但是真实的结果是: " + result);
    }

    public static synchronized void init(){
        if (!init){
            init = true;
            AviatorManager.getInstance().addFunction(new NotNullFunction());
            AviatorManager.getInstance().addFunction(new IsArrayFunction());
            AviatorManager.getInstance().addFunction(new IsMapFunction());
        }
    }

}
