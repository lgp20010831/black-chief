package com.black.core.sql.code.pattern;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.SQLListener;
import com.black.core.sql.annotation.TableName;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.GlobalSQLRunningListener;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.sqls.BoundStatement;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import lombok.extern.log4j.Log4j2;

import java.util.Set;

@Log4j2 @SQLListener
public class ConversionTableNameListener implements GlobalSQLRunningListener {

    @Override
    public void beforeProcessExecution(Configuration configuration, Object[] args) {
        MethodWrapper mw = configuration.getMethodWrapper();
        ParameterWrapper parameter = mw.getSingleParameterByAnnotation(TableName.class);
        if (parameter != null){
            Object arg = args[parameter.getIndex()];
            if (arg != null){
                String tn = arg.toString();
                log.info("switch table:[{}]", tn);
                Set<String> currentTables = TableUtils.getCurrentTables(configuration.getDatasourceAlias());
                if (!currentTables.contains(tn)){
                    throw new SQLSException("当前切换的表不存在, 表名：" + tn);
                }
                configuration.setTableName(tn);
                ParameterWrapper pw = mw.getSingleParameterByType(BoundStatement.class);
                if (pw != null){
                    Object o = args[pw.getIndex()];
                    if (o != null){
                        ConnectionManagement.employConnection(configuration.getDatasourceAlias(), connection -> {
                            TableMetadata tableMetadata = TableUtils.getTableMetadata(tn, connection);
                            BoundStatement statement = (BoundStatement) o;
                            statement.getStatement().resetTableName(tn);
                            statement.getStatement().calibration(tableMetadata);
                        });
                    }
                }
            }
        }
    }
}
