package com.black.core.sql.code.impl.config_impl;

import com.black.condition.def.DefaultConditional;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.ConditionUnit;
import com.black.core.sql.code.condition.ConditionSelector;
import com.black.core.sql.code.condition.SqlConditionEngine;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.inter.ConfigurationAnnotationResolver;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sup.SqlSequencesFactory;
import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;

import java.util.Map;

public abstract class AbstractConditionValueResolver implements ConfigurationAnnotationResolver {

    final BeanFactory factory;

    protected AbstractConditionValueResolver(BeanFactory factory) {
        this.factory = factory;
    }

    @Override
    public void doReolver(Configuration configuration, ExecutePacket ep) {
        beforeConditionParse(configuration, ep);
        MethodWrapper mw = configuration.getMethodWrapper();
        if (SqlConditionEngine.isConditional(configuration)){
            processorWhenOpenCondition(mw, configuration, ep);
        }
    }

    protected abstract void processorWhenOpenCondition(MethodWrapper mw, Configuration configuration, ExecutePacket ep);

    protected abstract void beforeConditionParse(Configuration configuration, ExecutePacket ep);

    protected void handlerSeq(String[] sqlSequences, Configuration configuration, SqlOutStatement statement, Map<String, Object> originalArgs){
        for (String sqlSequence : sqlSequences) {
            sqlSequence = GlobalMapping.parseAndObtain(sqlSequence, true);
            OperationType type = configuration.getMethodType() == SQLMethodType.INSERT ?
                    OperationType.INSERT : OperationType.SELECT;
            SqlSequencesFactory.parseSeq(statement, sqlSequence, type, originalArgs, configuration.getTableMetadata());
        }
    }

    protected void parseConditionSeq(ExecutePacket ep, MethodWrapper mw, Configuration configuration, ConditionUnit[] units){
        SqlOutStatement statement = ep.getStatement();
        Map<String, Object> originalArgs = ep.getOriginalArgs();
        for (ConditionUnit conditionUnit : units) {
            parseConditionUnit(ep, mw, configuration, conditionUnit);
        }
    }

    protected void parseConditionUnit(ExecutePacket ep, MethodWrapper mw, Configuration configuration, ConditionUnit unit){
        SqlOutStatement statement = ep.getStatement();
        Map<String, Object> originalArgs = ep.getOriginalArgs();
        String[] expressions = unit.expression();
        for (String expression : expressions) {
            if (!ConditionSelector.excCondition(expression, originalArgs)) {
                doElse(unit.orElse(), configuration, statement, originalArgs);
                return;
            }
        }
        if (DefaultConditional.doParse(unit.condition(), factory, originalArgs)) {
            handlerSeq(unit.then(), configuration, statement, originalArgs);
        }
    }

    protected void doElse(String[] elses, Configuration configuration, SqlOutStatement statement, Map<String, Object> originalArgs){
        handlerSeq(elses, configuration, statement, originalArgs);
    }
}
