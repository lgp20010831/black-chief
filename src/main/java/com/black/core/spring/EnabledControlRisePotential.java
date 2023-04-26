package com.black.core.spring;

import java.lang.annotation.Annotation;

public interface EnabledControlRisePotential {

    default Class<? extends Annotation> registerEnableAnnotation(){
        return null;
    }

    /** 当验证通过时 */
    default void postVerificationQualifiedDo(Annotation annotation,
                                     ChiefExpansivelyApplication application){}
}
