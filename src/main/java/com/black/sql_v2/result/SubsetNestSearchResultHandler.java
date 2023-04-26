package com.black.sql_v2.result;

import com.black.nest.Nest;
import com.black.nest.NestManager;
import com.black.core.log.IoLog;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.cascade.Strategy;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.AbstractStringSupporter;
import com.black.sql_v2.Environment;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.SqlV2Pack;
import com.black.sql_v2.transaction.NestedTransactionControlManager;
import com.black.sql_v2.utils.*;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class SubsetNestSearchResultHandler extends AbstractStringSupporter implements SqlResultHandler {

    public static final String PREFIX = "nest v2";

    public static Strategy strategy = Strategy.GROUP_BY;

    public SubsetNestSearchResultHandler() {
        super(PREFIX);
    }

    @Override
    public boolean supportStatement(SqlOutStatement statement) {
        return SqlV2Utils.isSelectStatement(statement);
    }

    @Override
    public void handlerResultList(SqlOutStatement statement, List<Map<String, Object>> dataList, Object param, SqlV2Pack pack) {
        Connection connection = pack.getConnection();
        Environment environment = pack.getEnvironment();
        IoLog log = environment.getLog();
        SqlExecutor executor = pack.getExecutor();
        Map<String, Object> env = pack.getEnv();
        List<Nest> nests = NestManager.queryNestByParent(statement.getTableName(), connection);
        TableMetadata tableMetadata = TableUtils.getTableMetadata(statement.getTableName(), connection);
        String name = executor.getName();
        boolean transactionStart = false;
        //获取所有要查询的关联表元数据
        Strategy nowStrategy = strategy;
        try {
            if (!NestedTransactionControlManager.isAtive(name)){
                transactionStart = true;
                NestedTransactionControlManager.registerTransaction(executor.getName());
            }
            loop: for (Nest nest : nests) {
                TableMetadata sonMetadata = nest.getSonMetadata(connection);
                SubsetInfo subsetInfo = new SubsetInfo(tableMetadata, sonMetadata);
                subsetInfo.setWatchword(PREFIX);
                subsetInfo.setMasterIdName(nest.getParentKey());
                subsetInfo.setSubIdName(nest.getSonKey());
                subsetInfo.setApplySql(nest.getApplySql());
                subsetInfo.setSuffix(nest.getSuffix());
                subsetInfo.setCreateBy(executor.getName());
                subsetInfo.setOneMany(nest.wasOneMany());
                String subTableName = subsetInfo.getSubTableName();
                String applySql = subsetInfo.getApplySql();
                if (applySql != null){
                    applySql = MapArgHandler.parseSql(applySql, env);
                    subsetInfo.setApplySql(applySql);
                }
                subsetInfo.convert(environment.getConvertHandler());
                for (SubsetQueryStrategyHandler strategyHandler : SubsetStrategyHandlerManager.getStrategyHandlers()) {
                    if (strategyHandler.support(nowStrategy)) {
                        log.info("[SQL] -- subset nest query handler processing sub table: [{}] -- strategy handle is [{}]",
                                subTableName, SqlV2Utils.getName(strategyHandler));
                        strategyHandler.handle(dataList, subsetInfo);
                        continue loop;
                    }
                }
            }

        }finally {
            if (transactionStart){
                NestedTransactionControlManager.closeTransaction(name);
            }
        }
    }
}
