package com.black.core.aop.servlet.time;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RepairTimeConfig {

    String startTimeName;

    String endTimeName;

    String appendStartTime;

    String appendEndTime;

    Class<? extends RepairTimePlug> plugClass;
}
