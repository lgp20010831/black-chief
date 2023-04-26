package com.black.core.sql.code.page;

import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.SQLListener;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.SQLSignalSession;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@SQLListener
public class PageListener implements GlobalSQLRunningListener {


    @Override
    public String postRunScriptSelectSql(GlobalSQLConfiguration configuration, String sql) {
        if (PageManager.isOpenPage()) {
            try {

                Page<?> page = PageManager.getPage();
                Integer pageNum = page.getPageNum();
                Integer pageSize = page.getPageSize();
                if (pageNum == null || pageSize == null){
                    //取消分页
                    return sql;
                }
                Connection connection = ConnectionManagement.getConnection(configuration.getDataSourceAlias());
                String countSql = processorSql(sql);
                ResultSet resultSet = SQLUtils.runQuery(countSql, connection);
                try {
                    while (resultSet.next()) {
                        page.setTotal(resultSet.getInt(1));
                    }
                    SQLUtils.closeResultSet(resultSet);
                } catch (SQLException e) {
                    throw new SQLSException(e);
                }
                //拼接分页 sql
                sql = StringUtils.linkStr(sql, " limit ", String.valueOf(pageSize),
                        " offset ", String.valueOf((pageNum - 1) * pageSize ));
                return sql;
            }finally {
                PageManager.close();
            }
        }
        return GlobalSQLRunningListener.super.postRunScriptSelectSql(configuration, sql);
    }

    @Override
    public String postQuerySql(Configuration configuration, String sql, List<SqlValueGroup> valueGroupList) {
        if (PageManager.isOpenPage()) {
            try {

                if (configuration instanceof AppearanceConfiguration){
                    return sql;
                }
                Page<?> page = PageManager.getPage();
                Integer pageNum = page.getPageNum();
                Integer pageSize = page.getPageSize();
                if (pageNum == null || pageSize == null){
                    //取消分页
                    return sql;
                }
                String countSql = processorSql(sql);
                SQLSignalSession session = configuration.getSession();
                ExecuteBody executeBody = session.pipelineSelect(countSql, valueGroupList);
                ResultSet queryResult = executeBody.getQueryResult();
                try {
                    while (queryResult.next()) {
                        page.setTotal(queryResult.getInt(1));
                    }
                    SQLUtils.closeResultSet(queryResult);
                } catch (SQLException e) {
                    throw new SQLSException(e);
                }
                //拼接分页 sql
                sql = StringUtils.linkStr(sql, " limit ", String.valueOf(pageSize),
                        " offset ", String.valueOf((pageNum - 1) * pageSize ));
                return sql;
            }finally {
                PageManager.close();
            }
        }
        return sql;
    }



    public String processorSql(String sql){
        return StringUtils.linkStr("select count(0) from ( ", sql, " ) s");
    }
}
