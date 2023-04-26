package com.black.ods;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.black.ods.OdsUtils.*;

public class QueryJdbcActuator extends AbstractJdbcActuator{

    private final OdsUndertake odsUndertake;

    private final String sql;

    public QueryJdbcActuator(OdsUndertake odsUndertake, String sql) {
        this.odsUndertake = odsUndertake;
        this.sql = sql;
    }

    @Override
    public OdsExecuteResult execute(OdsExecuteResult result, OdsChain chain) throws SQLException {
        OdsUndertakeConfiguation configuration = odsUndertake.getConfiguration();
        String[] paramInjectArray = configuration.getParamInjectArray();
        OdsConnectionManager connectionManager = chain.getConnectionManager();
        Connection connection = connectionManager.getConnection(odsUndertake);
        OdsExecuteResult executeResult = new OdsExecuteResult();
        if (result.isEmtryResult()){
            Object query = doExecuteQuery(sql, connection);
            executeResult.append((List<Map<String, Object>>) query);
        }else {
            List<Map<String, Object>> data = result.getResult();
            for (Map<String, Object> datum : data) {
                Object[] injectArgs = getInjectArgs(datum, paramInjectArray);
                Object query = doExecuteQuery(sql, connection, injectArgs);
                executeResult.append((List<Map<String, Object>>) query);
            }
        }
        return executeResult;
    }
}
