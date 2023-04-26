package com.black.sql_v2.result;

import com.black.sql.SqlOutStatement;
import com.black.sql_v2.ObjectParamSupporter;
import com.black.sql_v2.SqlV2Pack;

import java.util.List;
import java.util.Map;

public interface SqlResultHandler extends ObjectParamSupporter {

    boolean supportStatement(SqlOutStatement statement);

    default void handlerResultList(SqlOutStatement statement,
                                   List<Map<String, Object>> dataList,
                                   Object param,
                                   SqlV2Pack pack){}


}
