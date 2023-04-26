package com.black.core.aop.servlet.time;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepairTime {

    String startTimeName() default "startTime";

    String endTimeName() default "endTime";

    String appendStartTime() default " 00:00:00";

    String appendEndTime() default " 23:59:59";

    Class<? extends RepairTimePlug> plugClass() default NoOperationPlug.class;
}
