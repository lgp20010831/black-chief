package com.black.config.intercept;

import com.black.aviator.AviatorManager;
import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.config.annotation.When;
import com.black.core.query.FieldWrapper;

import java.util.Map;

@SuppressWarnings("all")
public class ConditionAttributeSetIntercept implements AttributeSetIntercept{
    @Override
    public boolean supportField(FieldWrapper fw) {
        return fw.hasAnnotation(When.class);
    }

    @Override
    public boolean interceptField(FieldWrapper fw, Object bean, ConfiguringAttributeAutoinjector autoinjector) {
        When annotation = fw.getAnnotation(When.class);
        Object dataSource = autoinjector.getDataSource();
        String[] texts = annotation.value();
        for (String text : texts) {
            Object execute = AviatorManager.execute(text, (Map<String, Object>) dataSource);
            if (!(execute instanceof Boolean)){
                throw new IllegalStateException("The result of the when expression is not a Boolean value");
            }
            Boolean result = (Boolean) execute;
            if (!result){
                return true;
            }
        }
        return false;
    }
}
