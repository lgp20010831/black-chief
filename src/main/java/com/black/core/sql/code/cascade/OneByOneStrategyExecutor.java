package com.black.core.sql.code.cascade;

import com.black.core.sql.code.AliasColumnConvertHandler;

import java.util.List;
import java.util.Map;

import static com.black.utils.ServiceUtils.getString;

public class OneByOneStrategyExecutor implements StrategyCascadeExecutor {
    @Override
    public boolean support(Strategy strategy) {
        return strategy == Strategy.ONE_BY_ONE;
    }

    @Override
    public List<Map<String, Object>> query(List<Map<String, Object>> masterDataList, CascadeGroup group, CascadeExecutor executor) {
        AliasColumnConvertHandler columnConvertHandler = group.getColumnConvertHandler();
        String itselfKey = executor.getItselfKey();
        String targetKey = executor.getTargetKey();
        String targetName = executor.getTargetName();
        String suffix = group.getSuffix();
        String mapKey = columnConvertHandler.convertAlias(targetName) + suffix;
        for (Map<String, Object> data : masterDataList) {
            String itselfValue = getString(data, columnConvertHandler.convertAlias(itselfKey));
            List<Map<String, Object>> subDataList = executor.addCondition(columnConvertHandler.convertAlias(targetKey), itselfValue).list();
            data.put(mapKey, subDataList);
        }
        return masterDataList;
    }
}
