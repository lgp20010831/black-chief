package com.black.sql_v2.handler;

import com.black.sql.SqlOutStatement;
import com.black.sql_v2.ObjectParamSupporter;
import com.black.sql_v2.SqlV2Pack;

import java.util.List;
import java.util.Map;

public interface SqlStatementHandler extends ObjectParamSupporter {

    boolean supportStatement(SqlOutStatement statement);

    SqlOutStatement handleStatement(SqlOutStatement statement, Object param);

    default void handlerResultList(SqlOutStatement statement,
                                   List<Map<String, Object>> dataList,
                                   Object param,
                                   SqlV2Pack pack){}

}
