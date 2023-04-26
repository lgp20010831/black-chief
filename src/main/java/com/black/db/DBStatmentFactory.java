package com.black.db;

import com.black.core.sql.code.StatementWrapper;
import com.black.table.TableUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class DBStatmentFactory {


    public static StatementWrapper getStatement(String sql, boolean query, boolean allowScroll, Connection connection){
        try {
            return new StatementWrapper(TableUtils.prepare(sql, query, allowScroll, connection));
        } catch (SQLException e) {
            throw new DBSqlException(e);
        }
    }


}
