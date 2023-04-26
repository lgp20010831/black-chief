package com.black.out_resolve.param;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;

public abstract class AbstractParamResolver implements ParamOutputStreamResolver{

    protected IoLog log = LogFactory.getArrayLog();

    protected String getStringValue(Object value){
        return value == null ? "" : value.toString();
    }

}
