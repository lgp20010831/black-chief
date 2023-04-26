package com.black.sql_v2.result;

import com.black.sql.SqlOutStatement;
import com.black.sql_v2.SqlV2Pack;
import com.black.sql_v2.print.QueryResultPrinter;
import com.black.sql_v2.utils.SqlV2Utils;

import java.util.List;
import java.util.Map;

public class QueryReusltPrinterHandler implements SqlResultHandler{

    @Override
    public boolean support(Object param) {
        return param instanceof QueryResultPrinter;
    }

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return SqlV2Utils.isSelectStatement(statement);
    }

    @Override
    public void handlerResultList(SqlOutStatement statement, List<Map<String, Object>> dataList, Object param, SqlV2Pack pack) {
        QueryResultPrinter printer = (QueryResultPrinter) param;
        if (dataList.isEmpty()){
            printer.printEmpty();
        }else {
            Map<String, Object> map = dataList.get(0);
            printer.printColumns(map.keySet());
            for (Map<String, Object> objectMap : dataList) {
                printer.printResultRow(objectMap.values());
            }
        }
    }
}
