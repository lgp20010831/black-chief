package com.black.xml.listener;

import com.black.core.sql.code.page.Page;
import com.black.core.sql.code.page.PageManager;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import com.black.sql.QueryResultSetParser;
import com.black.sql_v2.utils.SqlV2Utils;
import com.black.xml.XmlExecutor;

import java.sql.ResultSet;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-30 11:25
 */
@SuppressWarnings("all")
public class XmlSqlPageListener implements XmlSqlListener{

    @Override
    public String postSelectSql(String sql, Map<String, Object> env, XmlExecutor xmlExecutor) {
        if (PageManager.isOpenPage()){
            try {
                Page<?> page = PageManager.getPage();
                String selectCountSql = SqlV2Utils.wrapperSelectCountSql(sql);
                xmlExecutor.getLog().info("[SQL PAGE] ==> {}", selectCountSql);
                ResultSet resultSet = SQLUtils.runQuery(selectCountSql, xmlExecutor.getConnection());
                QueryResultSetParser parser = new QueryResultSetParser(resultSet);
                int total = parser.intVal();
                xmlExecutor.getLog().trace("[SQL PAGE] <== {}", total);
                page.setTotal(total);
                return SqlV2Utils.wrapperSelectSqlOfPage(sql, page.getPageSize(), page.getPageNum());
            }finally {
                PageManager.close();
            }
        }
        return sql;
    }
}
