package com.black.core.sql.code.impl.config_impl;


import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.ConditionSequence;
import com.black.core.sql.annotation.ConditionUnit;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.config.SyntaxManager;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.sql.SqlOutStatement;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SeqValueResolver extends AbstractConditionValueResolver {

    public SeqValueResolver(BeanFactory factory) {
        super(factory);
    }

    @Override
    protected void processorWhenOpenCondition(MethodWrapper mw, Configuration configuration, ExecutePacket ep) {
        if (mw.hasAnnotation(ConditionSequence.class)){
            ConditionSequence annotation = mw.getAnnotation(ConditionSequence.class);
            parseConditionSeq(ep, mw, configuration, annotation.value());
        }
        if (mw.hasAnnotation(ConditionUnit.class)){
            parseConditionSeq(ep, mw, configuration, new ConditionUnit[]{mw.getAnnotation(ConditionUnit.class)});
        }
    }

    @Override
    protected void beforeConditionParse(Configuration configuration, ExecutePacket ep) {
        SqlOutStatement statement = ep.getNhStatement().getStatement();
        Map<String, Object> originalArgs = ep.getOriginalArgs();

        synchronized (configuration.getSqlSequences()){
            Set<String> sqlSequences = getSqlSequences(configuration, ep);
            if (sqlSequences != null){
                handlerSeq(sqlSequences.toArray(new String[0]), configuration, statement, originalArgs);
            }
        }

    }

    private Set<String> getSqlSequences(Configuration configuration, ExecutePacket ep){
        Set<String> sqlSequences = configuration.getSqlSequences();

        List<String> list = SyntaxManager.callSyntax(configuration, ep, SyntaxConfigurer::getSequencesSyntaxs);
        if (list != null){
            sqlSequences.addAll(list);
        }

        List<String> localSyntax = SyntaxManager.localSyntax(configuration, SyntaxConfigurer::getSequencesSyntaxs);
        if(localSyntax != null){
            sqlSequences.addAll(localSyntax);
        }
        return sqlSequences;
    }

}
