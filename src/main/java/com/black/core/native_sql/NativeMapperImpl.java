package com.black.core.native_sql;

import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.sql.NativeMapper;
import com.black.sql.NativeSql;
import com.black.sql.Query;
import com.black.sql.QueryResultSetParser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static com.black.sql.NativeSql.DEFAULT_CONVERT_HANDLER;
import static com.black.sql.NativeSql.DEFAULT_LOG;

public class NativeMapperImpl implements NativeMapper {

    private final TransactionalDataSourceHandler transactionalDataSourceHandler;

    private final DataSource dataSource;

    public NativeMapperImpl(TransactionalDataSourceHandler transactionalDataSourceHandler, DataSource dataSource) {
        this.transactionalDataSourceHandler = transactionalDataSourceHandler;
        this.dataSource = dataSource;
    }

    @Override
    public List<Map<String, Object>> queryList0(String sql, Map<String, Object> env, Object... params) {
        Connection connection = transactionalDataSourceHandler.openConnection();
        Query query = new Query(sql);
        for (int i = 1; i <= params.length; i++) {
            query.setParam(i, params[i - 1]);
        }
        query.setConnection(connection);
        query.setCallback(conn -> {
            transactionalDataSourceHandler.closeConnection(connection);
        });
        query.setLog(DEFAULT_LOG);
        query.setEnv(env);
        QueryResultSetParser parser = query.execute();
        try{
            AliasColumnConvertHandler convertHandler = AliasStrategyThreadLocalManager.getConvertHandler();
            convertHandler = convertHandler == null ? DEFAULT_CONVERT_HANDLER : convertHandler;
            parser.setConvertHandler(convertHandler);
            return parser.list();
        }finally {
            AliasStrategyThreadLocalManager.remove();
        }

    }

    @Override
    public void update0(String sql, Map<String, Object> env, Object... params) {
        Connection connection = transactionalDataSourceHandler.openConnection();
        try {
            NativeSql.executeEnvUpdate(sql, connection, env, params);
        }finally {
            transactionalDataSourceHandler.closeConnection(connection);
        }

    }
}
