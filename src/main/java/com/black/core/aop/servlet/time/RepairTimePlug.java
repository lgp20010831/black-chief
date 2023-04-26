package com.black.core.aop.servlet.time;

import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.aop.servlet.ParameterWrapper;
import lombok.NonNull;

public interface RepairTimePlug {

    String repairStartTime(ParameterWrapper pw, @NonNull String notNullStartTime, Object[] args, HttpMethodWrapper mw) throws Throwable;

    String repairEndTime(ParameterWrapper pw, @NonNull String notNullEndTime, Object[] args, HttpMethodWrapper mw) throws Throwable;
}
