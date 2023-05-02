package com.black.sql_v2.listener;


import com.black.core.sql.code.page.Page;
import com.black.core.sql.code.page.PageManager;
import com.black.core.util.Assert;
import com.black.sql.QueryResultSetParser;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.utils.SqlV2Utils;

public class PageSqlListener implements SqlListener{

    @Override
    public String postInvokeSql(String sql, SqlOutStatement statement, SqlExecutor executor) {
        if (PageManager.isOpenPage() && SqlV2Utils.isSelectSql(sql)){
            try {
                Page<?> page = PageManager.getPage();
                String countSql = SqlV2Utils.wrapperSelectCountSql(sql);
                QueryResultSetParser parser = (QueryResultSetParser) executor.runSql(statement, countSql);
                Assert.notNull(parser, "run count select sql is should be not null");
                int total = parser.intVal();
                page.setTotal(total);
                return SqlV2Utils.wrapperSelectSqlOfPage(sql, page.getPageSize(), page.getPageNum());
            }finally {
                PageManager.close();
            }
        }
        return sql;
    }
}
