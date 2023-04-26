package com.black.core.sql.code.condition;

import com.black.condition.ConditionalEngine;
import com.black.condition.ConditionalHodler;
import com.black.condition.def.DefaultConditionalOnClass;
import com.black.condition.def.DefaultConditionalOnExpression;
import com.black.condition.def.DefaultConditionalOnValue;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.OpenConditional;
import com.black.core.sql.code.config.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SqlConditionEngine {

    static ConditionalEngine engine;

    static Map<Configuration, Boolean> conditionCache = new ConcurrentHashMap<>();

    public static ConditionalEngine getInstance(){
        if (engine == null){
            engine = ConditionalHodler.obtainEngine(list -> {
                list.remove(DefaultConditionalOnValue.class);
                list.remove(DefaultConditionalOnClass.class);
                list.remove(DefaultConditionalOnExpression.class);
                list.add(SqlConditionalOnClass.class);
                list.add(SqlConditionalOnExpression.class);
                list.add(SqlConditionalOnValue.class);
            }, "mapSql");
        }
        return engine;
    }


    public static boolean isConditional(Configuration configuration){
        return conditionCache.computeIfAbsent(configuration, cf -> {
            if (configuration.getGlobalSQLConfiguration().isOpenCondition()) {
                return true;
            }

            MethodWrapper mw = configuration.getMethodWrapper();
            if (mw.hasAnnotation(OpenConditional.class)) {
                return true;
            }

            return configuration.getCw().hasAnnotation(OpenConditional.class);
        });
    }


}
