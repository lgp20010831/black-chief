package com.black.core.aop.servlet.plus.parse;

import com.black.core.aop.servlet.plus.MybatisPlusAroundResolver;
import com.black.core.chain.*;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;
import java.util.HashSet;

@ChainClient(ArgumentParserScheduler.class)
@Adaptation(MybatisPlusAroundResolver.class)
public class ArgumentParserScheduler implements CollectedCilent, ChainPremise {

    public static final String ALIAS = "arg-parser";

    private final Collection<Object> parsers = new HashSet<>();

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin();
        entry.setAlias(ALIAS);
        entry.needOrder(false);
        entry.condition(c ->
            ArgumentParser.class.isAssignableFrom(c) && AnnotationUtils.getAnnotation(c, PlusArgumentParser.class) != null
        );
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if (resultBody.getAlias().equals(ALIAS)) {
            parsers.addAll(resultBody.getCollectSource());

        }
    }

    public Collection<Object> getParsers() {
        return parsers;
    }

    @Override
    public boolean premise() {
        try {
            Class.forName("com.baomidou.mybatisplus.core.conditions.Wrapper");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
