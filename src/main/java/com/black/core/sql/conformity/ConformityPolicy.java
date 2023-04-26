package com.black.core.sql.conformity;

import com.black.core.sql.code.config.Configuration;
import com.black.core.util.OrderlyMap;

public interface ConformityPolicy {

    boolean support(OrderlyMap<Configuration, Object> map);

    Object doConformity(OrderlyMap<Configuration, Object> map, OrderlyMap<Configuration, Object> queue);
}
