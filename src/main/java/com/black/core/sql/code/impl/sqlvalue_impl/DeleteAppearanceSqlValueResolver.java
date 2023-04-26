package com.black.core.sql.code.impl.sqlvalue_impl;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.ill.StopSqlInvokeException;
import com.black.core.sql.code.inter.SqlValueGroupHandler;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.session.SQLSignalSession;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.core.sql.code.sqls.MappingVal;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.SqlValue;
import com.black.core.sql.unc.SqlVariable;
import com.black.core.sql.unc.SqlWriter;
import com.black.core.sql.unc.OperationType;
import com.black.core.util.Assert;
import com.black.core.util.Utils;
import com.black.sql.SqlOutStatement;
import com.black.table.TableMetadata;

import java.util.*;


public class DeleteAppearanceSqlValueResolver implements SqlValueGroupHandler {
    @Override
    public boolean support(Configuration configuration) {
        return configuration instanceof AppearanceConfiguration &&
                configuration.getMethodType() == SQLMethodType.DELETE;
    }

    @Override
    public List<SqlValueGroup> handler(Configuration configuration, ExecutePacket ep) {
        AppearanceConfiguration ac = (AppearanceConfiguration) configuration;
        Map<String, Object> originalArgs = ep.getOriginalArgs();
        //找到之删除语句依赖的主键id们
        List<String> deleteIds = findDeleteIds(configuration, ep);
        //新构造的 update sql 语句
        SqlOutStatement statement = ep.getNhStatement().getStatement();
        //将条件设置为 主键 in(, , , )
        if(Utils.isEmpty(deleteIds)){
            statement.writeAftSeq(ac.getForeignKeyColumnName() + " is null ");
        }else {
            statement.writeIn(ac.getForeignKeyColumnName(), false, deleteIds);
        }
        statement.flush();
        return new ArrayList<>();
    }

    private List<String> findDeleteIds(Configuration configuration, ExecutePacket ep){
        AppearanceConfiguration ac = (AppearanceConfiguration) configuration;
        //拿到子表外键
        String foreignKeyColumnName = ac.getForeignKeyColumnName();
        //拿到主表主键
        String primaryKeyName = ac.getPrimaryKey().getName();
        //上一个处理的 ep 包
        ExecutePacket prveEp = ac.getEp();
        Assert.notNull(prveEp, "ep 不应该为空");
        List<String> ids = new ArrayList<>();
        //上一个更新的 update sql 语句
        BoundStatement nhStatement = prveEp.getNhStatement();
        //试图找到传递的 id 参数
        Collection<MappingVal> mappingValList = nhStatement.getMappingVals();
        for (MappingVal mappingVal : mappingValList) {
            if (primaryKeyName.equals(mappingVal.getColumnName()) && mappingVal.getOperationType() == OperationType.SELECT){
                Object paramValue = mappingVal.getParamValue();
                if (paramValue instanceof List){
                    List<Object> idList = (List<Object>) paramValue;
                    for (Object o : idList) {
                        ids.add(SQLUtils.getString(o));
                    }
                }else ids.add(SQLUtils.getString(paramValue));
            }
        }
        if (ids.isEmpty()){
            //则继续寻找主键信息
            ids.addAll(getRawId(nhStatement, ac));
        }
        return ids;
    }

    private List<String> getRawId(BoundStatement boundStatement, AppearanceConfiguration configuration){
        //外键
        String columnName = configuration.getForeignKeyColumnName();
        //主表表信息
        TableMetadata tableMetadata = configuration.getConfiguration().getTableMetadata();
        //主表主键信息
        String masterName = configuration.getPrimaryKey().getName();
        //获取之前更新 sql 语句
        SqlOutStatement statement = boundStatement.getStatement();
        //拿到映射参数值
        Collection<MappingVal> mappingValList = boundStatement.getMappingVals();
        //构造新的查询语句根据之前更新的语句的条件
        String sql = SqlWriter.select(configuration.getTableName())
                .writeAft(statement.getCheckAft())
                .toString();
        SQLSignalSession session = configuration.getSession();
        try {
            Log log = configuration.getGlobalSQLConfiguration().getLog();
            if (log.isDebugEnabled()) {
                log.debug("==> try get delete result, so do select: [" + sql + "]");
            }

            SqlValueGroup valueGroup = new SqlValueGroup();
            for (MappingVal mappingVal : mappingValList) {
                List<SqlVariable> variables = statement.getVariable(mappingVal.getColumnName(), mappingVal.getOperationType());
                if (variables.isEmpty()) continue;
                Object paramValue = mappingVal.getParamValue();
                if (variables.size() > 1 &&
                        paramValue instanceof List<?> &&
                        ((List<?>)paramValue).size() == variables.size()
                ){
                    List<?> paramList = (List<?>) paramValue;
                    for (int i = 0; i < variables.size(); i++) {
                        SqlValue sqlValue = new SqlValue(variables.get(i), paramList.get(i),
                                tableMetadata.getColumnMetadata(mappingVal.getColumnName()));
                        valueGroup.addValue(sqlValue);
                    }
                    continue;
                }
                if (variables.size() == 1){
                    SqlValue sqlValue = new SqlValue(variables.get(0), paramValue,
                            tableMetadata.getColumnMetadata(mappingVal.getColumnName()));
                    valueGroup.addValue(sqlValue);
                }
            }
            //执行查询语句
            ExecuteBody executeBody = session.pipelineSelect(sql, Collections.singletonList(valueGroup));
            //解析查询结果
            List<Map<String, Object>> maps = SQLUtils.parseResult(executeBody.getQueryResult(), configuration.getMasterMetadata());
            if (maps.isEmpty()){
                throw new StopSqlInvokeException("中断因为原更新 sql 没有更新任何数据");
            }
            List<String> masterIds = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                Object id = map.get(masterName);
                Assert.notNull(id, "主表的主键不能为空, 字键名: " + masterName + "== 数据: " + map);
                masterIds.add(SQLUtils.getString(id));
            }
            return masterIds;
        } catch (Throwable e) {
            if (e instanceof StopSqlInvokeException){
                throw (StopSqlInvokeException)e;
            }
            throw new SQLSException("由于需要添加依赖关系, 但是 wrapper 不存在主键的属性, " +
                    "于是尝试查询更新的所有数据, 获取主键, 查询过程中发生异常, 异常的 sql: " + sql, e);
        }
    }
}
