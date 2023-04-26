package com.black.core.aop.servlet.flow;

public enum FlowTimeUnit {

    SECONDS("秒"),
    HOURS("小时"),
    MINUTES("分钟"),
    MONTHS("月份"),
    YEARS("年"),
    DAYS("天");

    String desc;

    FlowTimeUnit(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
