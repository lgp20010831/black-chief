package com.black.core.sql.conformity;

import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.util.OrderlyMap;

import java.util.concurrent.LinkedBlockingQueue;

public class ConformityPolicyAllocator extends AbstractStatementConformity{

    final LinkedBlockingQueue<ConformityPolicy> policiesQueue = new LinkedBlockingQueue<>();

    @Override
    protected Object conformityManyResult(OrderlyMap<Configuration, Object> queue) {
        OrderlyMap<Configuration, Object> filter = queue.filter((c, r) -> {
            return !(c instanceof AppearanceConfiguration);
        }, true);

        for (ConformityPolicy policy : policiesQueue) {
            if (policy.support(filter)) {
                return policy.doConformity(filter, queue);
            }
        }
        return null;
    }

    public void add(ConformityPolicy policy){
        if (policy != null){
            policiesQueue.add(policy);
        }
    }
}
