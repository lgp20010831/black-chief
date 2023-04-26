package com.black.core.aop.servlet.flow;

import com.black.core.mvc.response.Response;

import java.util.function.Supplier;

import static com.black.GlobalVariablePool.*;

public class DefaultLimitResponse implements Supplier<Object> {
    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public Object get() {
        return new Response(HTTP_CODE_CURRENT_lIMITING, false, HTTP_MSG_CURRENT_lIMITING);
    }
}
