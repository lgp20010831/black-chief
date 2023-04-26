package com.black.sql_v2.utils;

import com.black.core.util.Av0;
import com.black.sql_v2.Sql;

import java.util.List;
import java.util.Map;

public class SqlV2SubInvoker {

    public static List<Map<String, Object>> invoke(SubsetInfo info,
                                                   Object value){
        Map<String, Object> condition = Av0.of(info.getSubIdAlias(), value);
        return Sql.query(info.getSubTableName(), condition, "$A: " + info.getApplySql()).list();
    }


}
