package com.black.core.sql.code.wrapper;

import lombok.Getter;
import lombok.Setter;

@Getter  @Setter
public class PowerWrapperConfiguration extends WrapperConfiguration {
    String[] requiredProperties;
    String[] condition;
    String[] setFields;
    String[] autoInjection;
    boolean injectionPriority;  //如果为 true 则 autoInjection 优先级大于传参优先级
}
