package com.black.core.sql.code.impl.prepare_impl;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.BlendsManager;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.parse.BlendObjects;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
public class UpdateAppearancePrepareResolver  extends AbstractAppearanceSqlValueResolver {

    @Override
    protected void processorFruitSet(List<Map<String, Object>> fruitSet,
                                     AppearanceConfiguration configuration,
                                     ExecutePacket ep) {
        final TableMetadata tableMetadata = configuration.getTableMetadata();
        final Set<String> nameSet = tableMetadata.getColumnNameSet();
        final BoundStatement boundStatement = ep.getNhStatement();
        SqlOutStatement statement = boundStatement.getStatement();
        String primaryName = configuration.getPrimaryName();
        Map<String, BlendObjects> objectsMap = BlendsManager.getAndParse(configuration.getMethodWrapper());
        Set<String> pointCondition = new HashSet<>();
        BlendObjects update = objectsMap.get("update");
        if (update != null){
            BlendObjects child = update.getChild(configuration.getTableName());
            if (child != null){
                pointCondition.addAll(child.getAttributes());
            }
        }
        AliasColumnConvertHandler handler = configuration.getColumnConvertHandler();
        for (Map<String, Object> map : fruitSet) {
            for (String alias : map.keySet()) {
                String column = handler.convertColumn(alias);
                if (nameSet.contains(column)){
                    //如果该属性是主键或者是外键, 则作为条件存在
                    if (pointCondition.contains(column) || configuration.isPrimary(column) || configuration.isforeignKey(column)){
                        if (!statement.exisOperation(column, OperationType.SELECT)){
                            statement.writeEq(column, "?", false);
                        }
                    }else {
                        if (statement.exisOperation(column, OperationType.UPDATE)) {
                            statement.replaceOperation(column, OperationType.UPDATE, "?", false);
                        }else {
                            statement.writeSetVariable(column, "?");
                        }
                    }
                }
            }
        }

        ep.attach(fruitSet);
    }

    @Override
    protected List<Map<String, Object>> findOperationFruitSet(AppearanceConfiguration configuration,
                                                              Map<String, Object> originalArgs,
                                                              ExecutePacket packet, List<String> generatedKeys) {
        Object result = SQLUtils.loopFind(originalArgs, configuration.getAppearanceName());
        if (result == null){
            log.info("can not find [{}] in arg resource", configuration.getAppearanceName());
        }
        String primaryName = configuration.getPrimaryName();
        List<Map<String, Object>> list = null;
        if (result != null){

            if (result instanceof Map){
                list = SQLUtils.wrapperList((Map<String, Object>) result);
            }else if (result instanceof List){
                list = (List<Map<String, Object>>) result;
            }else {
                throw new IllegalArgumentException(configuration.getAppearanceName() + " type is not allow");
            }
        }
        if (list != null){
            String foreignKeyColumnName = configuration.getForeignKeyColumnName();
            String alias = configuration.convertAlias(foreignKeyColumnName);
            for (Map<String, Object> map : list) {
                if (primaryName == null || !map.containsKey(alias)){
                    if (generatedKeys.size() != 1){
                        throw new SQLSException("FruitSet 中不存在主键值, 并且更新的外键值可能为多个或者 0 ");
                    }

                    map.put(alias, generatedKeys.get(0));
                }
            }
        }
        return list;
    }

    @Override
    public boolean support(Configuration configuration) {
        return configuration instanceof AppearanceConfiguration &&
                configuration.getMethodType() == SQLMethodType.UPDATE;
    }
}
