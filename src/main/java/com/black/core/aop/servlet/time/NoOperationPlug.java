package com.black.core.aop.servlet.time;

import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.ParameterWrapper;

public class NoOperationPlug implements RepairTimePlug{
    @Override
    public String repairStartTime(ParameterWrapper pw, String notNullStartTime, Object[] args, HttpMethodWrapper mw) {
        return notNullStartTime;
    }

    @Override
    public String repairEndTime(ParameterWrapper pw, String notNullEndTime, Object[] args, HttpMethodWrapper mw) {
        return notNullEndTime;
    }
}
