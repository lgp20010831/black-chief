package com.black.sql_v2.result;

import com.black.core.log.IoLog;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.cascade.Strategy;
import com.black.core.sql.code.parse.BlendObject;
import com.black.core.sql.code.parse.CharParser;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.AbstractStringSupporter;
import com.black.sql_v2.Environment;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.SqlV2Pack;
import com.black.sql_v2.transaction.NestedTransactionControlManager;
import com.black.sql_v2.utils.*;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import lombok.Getter;

import java.sql.Connection;
import java.util.*;

public class SubsetSearchResultHandler extends AbstractStringSupporter implements SqlResultHandler {

    public static final String PREFIX = "open nest";

    public static Strategy strategy = Strategy.GROUP_BY;

    public SubsetSearchResultHandler() {
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
        String txt = getTxt(param);
        ForeignTarget foreignTarget = new ForeignTarget(txt);
        TableMetadata tableMetadata = TableUtils.getTableMetadata(statement.getTableName(), connection);
        tableMetadata.findSubset(connection);
        //获取所有要查询的关联表元数据
        List<TableMetadata> metadataList = choseSubset(foreignTarget, tableMetadata.getSubsetMetadataList());
        Strategy nowStrategy = strategy;
        try {
            NestedTransactionControlManager.registerTransaction(executor.getName());
            loop: for (TableMetadata metadata : metadataList) {
                SubsetInfo subsetInfo = new SubsetInfo(tableMetadata, metadata);
                subsetInfo.setWatchword(PREFIX);
                subsetInfo.foreignKeyLookup();
                subsetInfo.setCreateBy(executor.getName());
                String subTableName = subsetInfo.getSubTableName();
                String applySql = foreignTarget.getApplySql(subTableName);
                if (applySql != null){
                    applySql = MapArgHandler.parseSql(applySql, env);
                    subsetInfo.setApplySql(applySql);
                }
                subsetInfo.convert(environment.getConvertHandler());
                for (SubsetQueryStrategyHandler strategyHandler : SubsetStrategyHandlerManager.getStrategyHandlers()) {
                    if (strategyHandler.support(nowStrategy)) {
                        log.info("[SQL] -- subset query handler processing sub table: [{}] -- strategy handle is [{}]",
                                subTableName, SqlV2Utils.getName(strategyHandler));
                        strategyHandler.handle(dataList, subsetInfo);
                        continue loop;
                    }
                }
            }
        }finally {
            NestedTransactionControlManager.closeTransaction(executor.getName());
        }
    }

    protected List<TableMetadata> choseSubset(ForeignTarget foreignTarget, Collection<TableMetadata> metadatas){
        List<TableMetadata> result = new ArrayList<>();
        if (!foreignTarget.isConfigExclude() && !foreignTarget.isConfigInclude()){
            result.addAll(metadatas);
        }else if(foreignTarget.isConfigInclude()){
            List<String> includeList = foreignTarget.getIncludeList();
            for (TableMetadata metadata : metadatas) {
                if (includeList.contains(metadata.getTableName())){
                    result.add(metadata);
                }
            }
        }else if (foreignTarget.isConfigExclude()){
            List<String> excludeList = foreignTarget.getExcludeList();
            for (TableMetadata metadata : metadatas) {
                if (!excludeList.contains(metadata.getTableName())){
                    result.add(metadata);
                }
            }
        }else {
            throw new IllegalStateException("can not also specify include and exclude");
        }
        return result;
    }

    @Getter
    public static class ForeignTarget{

        //exclude[],include[]
        private final String txt;

        private List<String> excludeList;

        private List<String> includeList;

        private final Map<String, String> applySqlMap = new HashMap<>();

        public ForeignTarget(String txt) {
            this.txt = txt;
            parseTxt();
        }
        public boolean isConfigInclude(){
            return includeList != null;
        }

        public boolean isConfigExclude(){
            return excludeList != null;
        }

        public String getApplySql(String name){
            return applySqlMap.get(name);
        }

        private void parseTxt(){
            List<BlendObject> blends = CharParser.parseBlend(txt);
            for (BlendObject blend : blends) {
                String name = blend.getName();
                if ("include".equalsIgnoreCase(name)){
                    blend.getAttributes().forEach(this::addInclude);
                }else
                if ("exclude".equalsIgnoreCase(name)){
                    blend.getAttributes().forEach(this::addExclude);
                }else {
                    List<String> attributes = blend.getAttributes();
                    String applySql = attributes.isEmpty() ? null : attributes.get(0);
                    if (applySql != null){
                        applySqlMap.put(name, applySql);
                    }
                }
            }
        }

        public void addInclude(String name){
            if (includeList == null){
                includeList = new ArrayList<>();
            }
            includeList.add(name);
        }

        public void addExclude(String name){
            if (excludeList == null){
                excludeList = new ArrayList<>();
            }
            excludeList.add(name);
        }
    }
}
