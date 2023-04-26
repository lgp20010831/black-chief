package com.black.core.sql.conformity;

import com.black.core.sql.code.config.Configuration;
import com.black.core.util.OrderlyMap;

public interface StatementConformity {

    //寄存结果
    void deposit(Configuration configuration, Object result);

    //整合结果
    Object conformityResult();

    //获取结果集列表
    OrderlyMap<Configuration, Object> getResultQueue();

    //清除结果集
    void clear();

    void add(ConformityPolicy policy);
}
