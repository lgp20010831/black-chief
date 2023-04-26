package com.black.sql_v2.utils;

import java.util.concurrent.LinkedBlockingQueue;

public class SubsetStrategyHandlerManager {

    private static final LinkedBlockingQueue<SubsetQueryStrategyHandler> strategyHandlers = new LinkedBlockingQueue<>();

    static {
        strategyHandlers.add(new GroupSubsetQueryStrategyHandler());
        strategyHandlers.add(new QueryOneByOneStrategyHandler());
    }

    public static LinkedBlockingQueue<SubsetQueryStrategyHandler> getStrategyHandlers() {
        return strategyHandlers;
    }
}
