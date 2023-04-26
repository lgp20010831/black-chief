package com.black.sql_v2.utils;

import com.black.core.sql.code.cascade.Strategy;
import com.black.core.util.Av0;
import com.black.sql_v2.Sql;
import com.black.utils.CollectionUtils;

import java.util.List;
import java.util.Map;

import static com.black.utils.ServiceUtils.getString;

public class QueryOneByOneStrategyHandler implements SubsetQueryStrategyHandler{
    @Override
    public boolean support(Strategy strategy) {
        return strategy == Strategy.ONE_BY_ONE;
    }

    @Override
    public void handle(List<Map<String, Object>> dataList, SubsetInfo subsetInfo) {
        String key = subsetInfo.getSubTableAlias() + subsetInfo.getSuffix();
        for (Map<String, Object> map : dataList) {
            String id = getString(map, subsetInfo.getMasterIdAlias());
            Map<String, Object> condition = Av0.of(subsetInfo.getSubIdAlias(), id);
            List<Map<String, Object>> list = Sql.opt(subsetInfo.getCreateBy()).query(subsetInfo.getSubTableName(), condition, "$A: " + subsetInfo.getApplySql(), subsetInfo.getWatchword()).list();
            Object mapValue = subsetInfo.isOneMany() ? list : CollectionUtils.firstElement(list);
            map.put(key, mapValue);
        }
    }
}
