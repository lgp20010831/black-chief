package com.black.core.sql.code.pattern;

import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.ReflexUtils;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.query.Array;
import com.black.core.query.AscArray;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.sql.annotation.InAdvance;
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
public class InAdvanceListener implements GlobalSQLRunningListener {

    @Override
    public void beforeProcessExecution(Configuration configuration, Object[] args) throws Throwable {
        MethodWrapper mw = configuration.getMethodWrapper();
        InAdvance inAdvance = mw.getAnnotation(InAdvance.class);
        if (inAdvance == null){
            inAdvance = configuration.getCw().getAnnotation(InAdvance.class);
        }

        if (inAdvance != null){
            SqlProvision provision = null;
            if (!SqlProvision.class.equals(inAdvance.provider())){
                InstanceFactory factory = FactoryManager.getInstanceFactory();
                if (factory != null){
                    provision = factory.getInstance(inAdvance.provider());
                }else {
                    provision = ReflexUtils.instance(inAdvance.provider());
                }
            }

            Connection connection = ConnectionManagement.getConnection(configuration.getGlobalSQLConfiguration().getDataSourceAlias());
            Array<String> sqls = new AscArray<>(inAdvance.value());
            if (provision != null){
                sqls.merge(provision.getSqlArray());
            }
            final Log log = configuration.getGlobalSQLConfiguration().getLog();
            final InAdvance finalInAdvance = inAdvance;
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
                        if (finalInAdvance.stopGlobalOnInvokeError()){
                            throw e;
                        }
                        if (finalInAdvance.stopOnInvokeError()){
                            break;
                        }
                    }
                }
                return null;
            }, configuration.getDatasourceAlias());
        }
    }
}
