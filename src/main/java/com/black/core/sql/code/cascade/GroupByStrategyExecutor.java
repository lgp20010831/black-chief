package com.black.core.sql.code.cascade;

import com.black.core.chain.GroupUtils;
import com.black.core.sql.code.AliasColumnConvertHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.black.utils.ServiceUtils.getString;

public class GroupByStrategyExecutor implements StrategyCascadeExecutor{
    @Override
    public boolean support(Strategy strategy) {
        return strategy == Strategy.GROUP_BY;
    }

    @Override
    public List<Map<String, Object>> query(List<Map<String, Object>> masterDataList, CascadeGroup group, CascadeExecutor executor) {
        AliasColumnConvertHandler columnConvertHandler = group.getColumnConvertHandler();
        String itselfKey = executor.getItselfKey();
        String targetKey = executor.getTargetKey();
        String targetName = executor.getTargetName();
        String suffix = group.getSuffix();
        String mapKey = columnConvertHandler.convertAlias(targetName) + suffix;
        List<Map<String, Object>> subDataList = executor.list();
        String targetKeyAlias = columnConvertHandler.convertAlias(targetKey);
        String itselfKeyAlias = columnConvertHandler.convertAlias(itselfKey);
        Map<String, List<Map<String, Object>>> groups = GroupUtils.groupArray(subDataList, map -> getString(map, targetKeyAlias));
        for (Map<String, Object> data : masterDataList) {
            String key = getString(data, itselfKeyAlias);
            List<Map<String, Object>> maps = groups.get(key);
            maps = maps == null ? new ArrayList<>() : maps;
            data.put(mapKey, maps);
        }
        return masterDataList;
    }
}
