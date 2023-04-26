package com.black.core.sql.code.result;

import com.black.core.sql.code.impl.result_impl.*;
import com.black.core.sql.code.inter.ExecuteResultResolver;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

public class ResultHandlerCollector {

    private static Collection<ExecuteResultResolver> resultResolvers;

    public static synchronized Collection<ExecuteResultResolver> getResultResolvers() {
        if (resultResolvers == null){
            resultResolvers = new LinkedBlockingQueue<>();
            resultResolvers.add(new BooleanResultResolver());
            resultResolvers.add(new NumberListResultHandler());
            resultResolvers.add(new MapSelectResultHandler());
            resultResolvers.add(new MapInsertOrUpdateResultHandler());
            resultResolvers.add(new StringInsertUpdateResultHandler());
            resultResolvers.add(new StringSelectResultHandler());
        }
        return resultResolvers;
    }
}
