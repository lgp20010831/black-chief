package com.black.core.sql.code.listener;

import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.UpdateOrInsertConifg;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.sqls.ResultSetThreadManager;
import com.black.core.sql.code.sqls.ResultType;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.code.sup.SqlSequencesFactory;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.core.sql.unc.SqlValue;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.table.TableMetadata;
import com.black.utils.LocalSet;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class UpdateMasterProcessor extends AbstractProcessor{

    private final LocalSet<String> retrievalSet = new LocalSet<>();

    /**
     * 更新传值的所有情况:
     * 当传值为 xxxList: {} 粗略认为 主表与从表关系 1 - 1
     *
     * 当传值为 xxxList:[] 则可以证明主表与从表 1 - n
     *      1.{no id}, {no id} 对于没有主键约束的值, 或者能够判断的值, 则认为每条数据都为添加
     *      2.{no id}, {id} 对于有主键约束和没有主键约束的, 同样采用相同策略, 有主键则去数据库正常寻找, 否则认为作为添加
     *      3.{id}, {id} 相同策略
     *
     */

    public void clearRetrieval(){
        retrievalSet.clear();
    }

    boolean updateExist(String sql, SqlValueGroup sqlValueGroup,
                        Configuration configuration, UpdateOrInsertConifg conifg) throws SQLException {
        List<SqlValue> selectSqlValues = sqlValueGroup.find(OperationType.SELECT);
        int count = sqlValueGroup.getCount(OperationType.UPDATE);
        if (configuration instanceof AppearanceConfiguration){
            //拿到映射的外键名
            String foreignKeyColumnName = ((AppearanceConfiguration) configuration).getForeignKeyColumnName();
            String primaryName = configuration.getPrimaryName();
            if (foreignKeyColumnName != null && primaryName != null){
                SqlValue sqlValue = sqlValueGroup.findSingle(foreignKeyColumnName, OperationType.SELECT);
                SqlValue primary = sqlValueGroup.findSingle(primaryName, OperationType.SELECT);
                //是根据外键查的, 并且没有根据主键一起查
                if (sqlValue != null && primary == null){
                    String foreignValue = sqlValue.getValue().toString();
                    if (retrievalSet.contains(foreignValue)){
                        return false;
                    }else {
                        retrievalSet.add(foreignValue);
                    }
                }
            }
        }
        String selectSql = createSelectSql(sql, configuration, conifg);
        reduce(selectSqlValues, count);
        boolean exist = false;
        try {
            if (exist(selectSql, configuration, selectSqlValues)){
                exist = true;
            }
        }catch (SQLException e){
            throw e;
        }finally {
            recovery(selectSqlValues, count);
        }
        return exist;
    }

    //如果当前更新方法, 我们要将数据整合然后添加进去
    void processorUpdate(String sql, SqlValueGroup sqlValueGroup,
                         Configuration configuration,
                         UpdateOrInsertConifg conifg,
                         Connection connection,
                         ExecutePacket ep) throws SQLException {
        SqlOutStatement statement = SqlWriter.insert(configuration.getTableName());
        TableMetadata tableMetadata = configuration.getTableMetadata();

        Map<String, Object> originalArgs = ep.getOriginalArgs();
        synchronized (conifg.getSqlSeqs()){
            for (String sqlSeq : conifg.getSqlSeqs()) {
                sqlSeq = GlobalMapping.parseAndObtain(sqlSeq, true);
                SqlSequencesFactory.parseSeq(statement, sqlSeq, OperationType.INSERT, originalArgs, tableMetadata);
            }
        }

        for (String columnName : tableMetadata.getColumnNameSet()) {
            SqlValue value = sqlValueGroup.findSingle(columnName);
            if (value != null) {
                String val = SQLUtils.getString(value.getValue());
                if (statement.exisOperation(columnName, OperationType.INSERT)) {
                    statement.replaceOperation(columnName, OperationType.INSERT, val, false);
                } else {
                    statement.insertValue(columnName, val, false);
                }
            }
        }

        statement.calibration(configuration.getTableMetadata());
        statement.flush();
        String executeSql = statement.toString();
        Log log = configuration.getGlobalSQLConfiguration().getLog();
        if (log.isDebugEnabled()){
            log.debug("==> update or insert:[" + executeSql + "]");
        }
        Statement jdbcStatement = SQLUtils.callSql(executeSql, connection);
        ResultSetThreadManager.getResultAndParse(ResultType.GeneratedKeys, jdbcStatement.getGeneratedKeys());
    }
}
