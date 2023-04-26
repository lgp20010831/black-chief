package com.black.core.sql.code.cascade;

import java.util.List;
import java.util.Map;

public interface StrategyCascadeExecutor {


    boolean support(Strategy strategy);

    List<Map<String, Object>> query(List<Map<String, Object>> masterDataList, CascadeGroup group, CascadeExecutor executor);
}
