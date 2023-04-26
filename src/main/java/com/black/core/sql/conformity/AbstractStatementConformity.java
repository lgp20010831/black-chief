package com.black.core.sql.conformity;

import com.black.core.sql.code.config.Configuration;
import com.black.core.util.OrderlyMap;

public abstract class AbstractStatementConformity implements StatementConformity{

    private final OrderlyMap<Configuration, Object> resultQueue = new OrderlyMap<>();

    @Override
    public void deposit(Configuration configuration, Object result) {
        if (result != null && configuration != null)
            resultQueue.put(configuration, result);
    }

    @Override
    public Object conformityResult() {

        if (resultQueue.isEmpty()){
            return null;
        }

        if (resultQueue.size() == 1){
            return resultQueue.firstElement();
        }

        return conformityManyResult(resultQueue);
    }

    protected abstract Object conformityManyResult(OrderlyMap<Configuration, Object> queue);

    @Override
    public OrderlyMap<Configuration, Object> getResultQueue() {
        return resultQueue;
    }

    @Override
    public void clear() {
        resultQueue.clear();
    }
}
