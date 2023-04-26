package com.black.core.sql.code.pattern;

import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.ReflexUtils;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.query.Array;
import com.black.core.query.AscArray;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.sql.annotation.PostExecution;
import com.black.core.sql.annotation.SQLListener;
import com.black.core.sql.code.*;
import com.black.core.sql.code.advance.SqlProvision;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.util.SQLUtils;

import java.sql.Connection;
import java.util.Map;

@SQLListener
public class PostExecutionListener implements GlobalSQLRunningListener {

    @Override
    public void afterProcessExecution(Configuration configuration, Object[] args) throws Throwable {
        MethodWrapper mw = configuration.getMethodWrapper();
        PostExecution postExecution = mw.getAnnotation(PostExecution.class);
        if (postExecution == null){
            postExecution = configuration.getCw().getAnnotation(PostExecution.class);
        }

        if (postExecution != null){
            SqlProvision provision = null;
            if (!SqlProvision.class.equals(postExecution.provider())){
                InstanceFactory factory = FactoryManager.getInstanceFactory();
                if (factory != null){
                    provision = factory.getInstance(postExecution.provider());
                }else {
                    provision = ReflexUtils.instance(postExecution.provider());
                }
            }

            Connection connection = ConnectionManagement.getConnection(configuration.getGlobalSQLConfiguration().getDataSourceAlias());
            Array<String> sqls = new AscArray<>(postExecution.value());
            if (provision != null){
                sqls.merge(provision.getSqlArray());
            }
            TransactionHandler handler = TransactionSQLManagement.getTransactionConnection(configuration
                    .getGlobalSQLConfiguration().getDataSourceAlias());
            Log log = configuration.getGlobalSQLConfiguration().getLog();
            final PostExecution finalPostExecution = postExecution;
            TransactionSQLManagement.transactionCall(() ->{
                Map<String, Object> objectMap = MapArgHandler.parse(args, mw);
                for (String sql : sqls) {
                    try {
                        sql = GlobalMapping.parseAndObtain(sql, true);
                        sql = MapArgHandler.parseSql(sql, objectMap);
                        if (log.isDebugEnabled()) {
                            log.debug("==> inadvance sql: [" + sql + "]");
                        }
                        SQLUtils.runSql(sql, connection);
                    }catch (RuntimeException e){
                        CentralizedExceptionHandling.handlerException(e);
                        if (finalPostExecution.stopGlobalOnInvokeError()){
                            throw e;
                        }
                        if (finalPostExecution.stopOnInvokeError()){
                            break;
                        }
                    }
                }
                return null;
            }, configuration.getDatasourceAlias());
        }
    }
}
