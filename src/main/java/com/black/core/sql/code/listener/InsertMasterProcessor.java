package com.black.core.sql.code.listener;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.UpdateOrInsertConifg;
import com.black.core.sql.code.ill.StopSqlInvokeException;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.packet.ExecutePacket;
import com.black.core.sql.code.sqls.ResultSetThreadManager;
import com.black.core.sql.code.sqls.ResultType;
import com.black.core.sql.code.sqls.SqlValueGroup;
import com.black.core.sql.code.sup.SqlSequencesFactory;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.unc.OperationType;
import com.black.core.sql.unc.SqlValue;
import com.black.core.util.Assert;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InsertMasterProcessor extends AbstractProcessor{


    private final ThreadLocal<SqlOutStatement> insertExistQuerySql = new ThreadLocal<>();

    boolean insertExist(String sql, SqlValueGroup sqlValueGroup,
                        Configuration configuration, UpdateOrInsertConifg conifg) throws SQLException {
        TableMetadata tableMetadata = configuration.getTableMetadata();

        Set<String> addingExist = conifg.getAddingExist();
        SqlOutStatement sqlStatement = SqlWriter.selectCount(tableMetadata.getTableName());
        if (addingExist.isEmpty()){
            PrimaryKey primaryKey = tableMetadata.firstPrimaryKey();
            Assert.notNull(primaryKey, "no primary in table: " + tableMetadata.getTableName());
            SqlValue single = sqlValueGroup.findSingle(primaryKey.getName(), OperationType.INSERT);
            if (single == null){
                throw new StopSqlInvokeException("无法找到主键的值, 并且没有其他判断条件");
            }
            sqlStatement.writeEq(primaryKey.getName(), SQLUtils.getString(single.getValue()), false);
        }else {
            synchronized (conifg.getAddingExist()){
                for (String column : addingExist) {
                    SqlValue single = sqlValueGroup.findSingle(column, OperationType.INSERT);
                    if (single == null){
                        throw new StopSqlInvokeException("插入的数据中没有指定的判断条件字段: " + column);
                    }
                    sqlStatement.writeEq(column, SQLUtils.getString(single.getValue()), false);
                }
            }
        }
        synchronized (conifg.getExistCondition()){
            for (String ec : conifg.getExistCondition()) {
                String obtain = GlobalMapping.parseAndObtain(ec);
                sqlStatement.writeAft("and");
                sqlStatement.writeAft(obtain);
            }
        }
        sqlStatement.flush();
        boolean exist = exist(sqlStatement.toString(), configuration, new ArrayList<>());
        insertExistQuerySql.set(sqlStatement);
        return exist;
    }

    void processorInsert(String sql, SqlValueGroup sqlValueGroup,
                         Configuration configuration,
                         UpdateOrInsertConifg conifg,
                         Connection connection,
                         ExecutePacket ep) throws SQLException {
        SqlOutStatement selectSqlStatement = insertExistQuerySql.get();
        if (selectSqlStatement == null){
            throw new SQLSException("查询添加的数据是否存在的 sql 语句丢失了");
        }
        try {
            SqlOutStatement statement = SqlWriter.update(configuration.getTableName());
            List<SqlValue> insertValues = sqlValueGroup.find(OperationType.INSERT);
            Map<String, Object> originalArgs = ep.getOriginalArgs();
            for (SqlValue insertValue : insertValues) {
                statement.writeSet(insertValue.getColumnMetadata().getName(), SQLUtils.getString(insertValue.getValue()), false);
            }
            synchronized (conifg.getSqlSeqs()){
                for (String sqlSeq : conifg.getSqlSeqs()) {
                    sqlSeq = GlobalMapping.parseAndObtain(sqlSeq, true);
                    SqlSequencesFactory.parseSeq(statement, sqlSeq, OperationType.UPDATE, originalArgs, configuration.getTableMetadata());
                }
            }
            statement.writeAft(selectSqlStatement.toAftString());
            statement.calibration(configuration.getTableMetadata());
            statement.flush();
            String executeSql = statement.toString();
            Log log = configuration.getGlobalSQLConfiguration().getLog();
            if (log.isDebugEnabled()){
                log.debug("==> update or insert:[" + executeSql + "]");
            }
            Statement jdbcStatement = SQLUtils.callSql(executeSql, connection);
            ResultSetThreadManager.getResultAndParse(ResultType.GeneratedKeys, jdbcStatement.getGeneratedKeys());
        }finally {
            insertExistQuerySql.remove();
        }
    }


}
