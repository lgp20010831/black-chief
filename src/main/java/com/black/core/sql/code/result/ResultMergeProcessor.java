package com.black.core.sql.code.result;

import com.black.core.chain.GroupUtils;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.config.AppearanceConfiguration;

import java.util.*;

public class ResultMergeProcessor {


    public void doParse(Collection<Map<String, Object>> m, Object sub, AppearanceConfiguration configuration){
        if (m == null || sub == null) return;
        AliasColumnConvertHandler handler = configuration.getColumnConvertHandler();
        Collection<Map<String, Object>> s = (Collection<Map<String, Object>>) sub;
        Map<Object, List<Map<String, Object>>> array = GroupUtils.groupArray(s, mp -> {
            return mp.get(handler.convertAlias(configuration.getForeignKeyColumnName()));
        });
        for (Map<String, Object> map : m) {
            Object id = map.get(configuration.getTargetKey());
            if (id != null){
                List<Map<String, Object>> list = array.get(id);
                map.put(configuration.getAppearanceName(), list == null ? new ArrayList<>() : list);
            }
        }
    }

}
