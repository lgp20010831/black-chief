package com.black.core.sql.code.session;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.StatementWrapper;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.table.TableUtils;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PrepareStatementFactory {

    private static Map<String, StatementWrapper> cache = new ConcurrentHashMap<>();

    public static StatementWrapper getStatement(String sql,
                                                GlobalSQLConfiguration configuration,
                                                Connection connection,
                                                boolean query) {
        return getStatement(sql, configuration.isUseStatementCache(), configuration.isAllowScroll(), connection, query);
    }

    public static StatementWrapper getStatement(String sql,
                                                boolean useCache,
                                                boolean allowScroll,
                                                Connection connection,
                                                boolean query){
        if (!useCache) {
            try {
                return doCreateStatement(sql, allowScroll, connection, query);
            } catch (SQLException e) {
                throw new SQLSException(e);
            }
        }else {
            return cache.computeIfAbsent(sql, sq -> {
                try {
                    return doCreateStatement(sql, allowScroll, connection, query);
                } catch (SQLException e) {
                    throw new SQLSException(e);
                }
            });
        }
    }

    static StatementWrapper doCreateStatement(String sql,
                                              boolean allowScroll,
                                              Connection connection,
                                              boolean query) throws SQLException {
        return new StatementWrapper(TableUtils.prepare(sql, query, allowScroll, connection));
    }


}
