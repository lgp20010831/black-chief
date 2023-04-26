package com.black.core.convert;

import com.black.core.chain.CollectedCilent;
import com.black.core.chain.InstanceClientAdapter;
import com.black.core.spring.ChiefExpansivelyApplication;


public class GetTypeComponentAdapter implements InstanceClientAdapter {

    private final ChiefExpansivelyApplication application;

    public GetTypeComponentAdapter(ChiefExpansivelyApplication application) {
        this.application = application;
    }

    @Override
    public CollectedCilent getClient() {
        return application.queryComponent(GlobalTypeConvertComponent.class);
    }
}
