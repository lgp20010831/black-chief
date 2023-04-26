package com.black.ods;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.black.ods.OdsUtils.doExecuteUpdate;
import static com.black.ods.OdsUtils.getInjectArgs;

public class UpdateJdbcActuator extends AbstractJdbcActuator{

    private final OdsUndertake odsUndertake;

    private final String sql;

    public UpdateJdbcActuator(OdsUndertake odsUndertake, String sql) {
        this.odsUndertake = odsUndertake;
        this.sql = sql;
    }

    @Override
    public OdsExecuteResult execute(OdsExecuteResult result, OdsChain chain) throws SQLException {
        OdsUndertakeConfiguation configuration = odsUndertake.getConfiguration();
        String[] paramInjectArray = configuration.getParamInjectArray();
        OdsConnectionManager connectionManager = chain.getConnectionManager();
        Connection connection = connectionManager.getConnection(odsUndertake);
        if (result.isEmtryResult()){
            doExecuteUpdate(sql, connection);
        }else {
            List<Map<String, Object>> data = result.getResult();
            for (Map<String, Object> datum : data) {
                Object[] injectArgs = getInjectArgs(datum, paramInjectArray);
                doExecuteUpdate(sql, connection, injectArgs);
            }
        }
        return new OdsExecuteResult(null);
    }


}
