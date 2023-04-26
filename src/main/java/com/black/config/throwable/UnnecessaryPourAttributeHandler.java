package com.black.config.throwable;

import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.config.annotation.Unnecessary;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;

public class UnnecessaryPourAttributeHandler implements BeanAttributeThrower{
    @Override
    public boolean supportField(FieldWrapper fw) {
        return fw.hasAnnotation(Unnecessary.class) || fw.getDeclaringClass().hasAnnotation(Unnecessary.class);
    }

    @Override
    public void handlerThrowableField(FieldWrapper fw, Object bean, Throwable ex, ConfiguringAttributeAutoinjector autoinjector) throws Throwable {

    }

    @Override
    public boolean supportMethod(MethodWrapper mw) {
        return mw.hasAnnotation(Unnecessary.class) ||
                mw.getDeclaringClassWrapper().hasAnnotation(Unnecessary.class);

    }
}
