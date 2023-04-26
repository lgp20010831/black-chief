package com.black.core.sql.code.impl.config_impl;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.ConditionSetValue;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.config.SyntaxManager;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sup.SqlSequencesFactory;
import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SetValuesResolver extends AbstractConditionValueResolver {
    public SetValuesResolver(BeanFactory factory) {
        super(factory);
    }

    @Override
    protected void processorWhenOpenCondition(MethodWrapper mw, Configuration configuration, ExecutePacket ep) {
        if (mw.hasAnnotation(ConditionSetValue.class)){
            ConditionSetValue annotation = mw.getAnnotation(ConditionSetValue.class);
            parseConditionSeq(ep, mw, configuration, annotation.value());
        }
    }

    @Override
    protected void beforeConditionParse(Configuration configuration, ExecutePacket ep) {
        if (configuration.getMethodType() != SQLMethodType.UPDATE){
            return;
        }
        SqlOutStatement statement = ep.getNhStatement().getStatement();
        Map<String, Object> originalArgs = ep.getOriginalArgs();
        synchronized (configuration.getSetValues()){
            Set<String> setValues = getSetValues(configuration, ep);
            //遍历set属性
            for (String setValue : setValues) {
                setValue = GlobalMapping.parseAndObtain(setValue, true);
                SqlSequencesFactory.parseSeq(statement, setValue, OperationType.UPDATE, originalArgs, configuration.getTableMetadata());
            }
        }
    }

    private Set<String> getSetValues(Configuration configuration, ExecutePacket ep){
        Set<String> setValues = configuration.getSetValues();

        List<String> list = SyntaxManager.callSyntax(configuration, ep, SyntaxConfigurer::getSetSyntaxs);
        if (list != null){
            setValues.addAll(list);
        }

        List<String> localSyntax = SyntaxManager.localSyntax(configuration, SyntaxConfigurer::getSetSyntaxs);
        if(localSyntax != null){
            setValues.addAll(localSyntax);
        }
        return setValues;
    }
}
