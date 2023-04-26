package com.black.core.sql.code.impl.prepare_impl;

import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.ill.StopSqlInvokeException;
import com.black.core.sql.code.inter.PrepareFinishResolver;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.sqls.*;
import com.black.core.util.Assert;
import com.black.core.util.Utils;
import com.black.sql.SqlOutStatement;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract class AbstractAppearanceSqlValueResolver implements PrepareFinishResolver {


    public boolean handler(Configuration configuration, ExecutePacket ep, SqlOutStatement statement){
        AppearanceConfiguration config = (AppearanceConfiguration) configuration;
        Map<String, Object> originalArgs = ep.getOriginalArgs();

        try {
            List<String> generatedKeys = findGeneratedKeys(config, ep);
            List<Map<String, Object>> operationFruitSet = findOperationFruitSet(config, originalArgs, ep, generatedKeys);
            if (!Utils.isEmpty(operationFruitSet)){

                //存在结果集: 表名 + 后缀
                processorFruitSet(operationFruitSet, config, ep);
                return false;
            }

            if (config.isRelyAppearance()){
                return true;
            }
            if(Utils.isEmpty(generatedKeys)){
                ep.getStatement().writeAftSeq(config.getForeignKeyColumnName() + " is null ");
            }else {
                ep.getStatement().writeIn(config.getForeignKeyColumnName(), true, generatedKeys);
            }

        } catch (SQLException e) {
            throw new StopSqlInvokeException("中断因为无法获取操作的主键结果集");
        }
        return false;
    }


    //获取上一次更新或添加操作后影响的主键
    private List<String> findGeneratedKeys(AppearanceConfiguration configuration, ExecutePacket ep) throws SQLException {
        //上一个处理的 ep 包
        ExecutePacket prveEp = configuration.getEp();
        ExecuteBody body = prveEp.getRp().getExecuteBody();
        Object result = ResultSetThreadManager.getResultAndParse(ResultType.GeneratedKeys, body.getWrapper().getGeneratedKeys());
        Assert.notNull(result, "no handler parse GeneratedKeys");
        return (List<String>) result;
    }

    protected abstract void processorFruitSet(List<Map<String, Object>> fruitSet,
                                                             AppearanceConfiguration configuration,
                                                             ExecutePacket ep);

    //寻找数据子集
    protected abstract List<Map<String, Object>> findOperationFruitSet(AppearanceConfiguration configuration,
                                                                       Map<String, Object> originalArgs,
                                                                       ExecutePacket packet, List<String> generatedKeys );

}
