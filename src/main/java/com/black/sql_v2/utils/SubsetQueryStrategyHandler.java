package com.black.sql_v2.utils;

import com.black.core.sql.code.cascade.Strategy;

import java.util.List;
import java.util.Map;

public interface SubsetQueryStrategyHandler {


    boolean support(Strategy strategy);

    void handle(List<Map<String, Object>> dataList, SubsetInfo subsetInfo);

}
