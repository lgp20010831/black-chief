package com.black.sql_v2.utils;

import com.black.core.chain.GroupUtils;
import com.black.core.sql.code.cascade.Strategy;
import com.black.sql_v2.Sql;
import com.black.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.black.utils.ServiceUtils.getString;

public class GroupSubsetQueryStrategyHandler implements SubsetQueryStrategyHandler{
    @Override
    public boolean support(Strategy strategy) {
        return strategy == Strategy.GROUP_BY;
    }

    @Override
    public void handle(List<Map<String, Object>> dataList, SubsetInfo subsetInfo) {
        List<Map<String, Object>> subLists = Sql.opt(subsetInfo.getCreateBy()).query(subsetInfo.getSubTableName(), "$A: " + subsetInfo.getApplySql(), subsetInfo.getWatchword()).list();
        Map<String, List<Map<String, Object>>> groupArray = GroupUtils.groupArray(subLists, map -> getString(map, subsetInfo.getSubIdAlias()));
        for (Map<String, Object> map : dataList) {
            String id = getString(map, subsetInfo.getMasterIdAlias());
            List<Map<String, Object>> list = groupArray.get(id);
            list = list == null ? new ArrayList<>() : list;
            Object mapValue = subsetInfo.isOneMany() ? list : CollectionUtils.firstElement(list);
            map.put(subsetInfo.getSubTableAlias() + subsetInfo.getSuffix(), mapValue);
        }
    }
}
