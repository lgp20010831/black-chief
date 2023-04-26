package com.black.core.aop.servlet.result;

import com.black.core.aop.servlet.RestResponse;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;

public interface ChiefBeforeWriteResolver {


    RestResponse resolver(RestResponse response, Object[] args, MethodWrapper mw, ClassWrapper<?> cw);


}
