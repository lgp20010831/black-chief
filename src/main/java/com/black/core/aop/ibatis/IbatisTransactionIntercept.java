package com.black.core.aop.ibatis;

import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.HijackObject;
import com.black.core.mybatis.source.DynamicallySqlSessionUtil;
import com.black.core.mybatis.source.DynamicallyTransactionManager;
import com.black.core.mybatis.source.TransactionAopAdministrationWrapper;
import com.black.core.mybatis.source.annotation.DynamicallySimpleRollBackTransactional;
import com.black.core.util.Assert;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class IbatisTransactionIntercept implements AopTaskIntercepet {

    private final IbatisRollBackHandler ibatisRollBackHandler;

    public IbatisTransactionIntercept() {
        ibatisRollBackHandler = new IbatisRollBackHandler();
    }

    private final Map<Method, TransactionAopAdministrationWrapper> wrapperMap = new ConcurrentHashMap<>();

    @Override
    public Object processor(HijackObject hijack) throws Throwable {

        if (log.isInfoEnabled()) {
            log.info("dynamically Transaction...");
        }
        Object[] args = hijack.getArgs();
        Method method = hijack.getMethod();
        TransactionAopAdministrationWrapper transactionAopAdministrationWrapper
                = wrapperMap.computeIfAbsent(method, this::createWrapper);
        boolean startingPoint = false;
        Object result;
        try {

            TransactionAopAdministrationWrapper currentAopTransaction = DynamicallyTransactionManager.getCurrentAopTransaction();
            if (currentAopTransaction == null){
                startingPoint = true;
                //注册 aop 事务管理
                DynamicallyTransactionManager.registerAopTransaction(transactionAopAdministrationWrapper);
            }
            args = ibatisRollBackHandler.beforeInvoke(hijack, method, args);
            result = hijack.doRelease(args);
        }catch (Throwable e){
            if (log.isInfoEnabled()) {
                log.info("Transaction detected error...");
            }
            ibatisRollBackHandler.handlerException(e, hijack, method);
            throw e;
        }finally {
            if (startingPoint){
                //应该先提交 sql session 然后关联连接
                ibatisRollBackHandler.commit(hijack, method);
                //脱离事务控制, 关闭 sqlSession
                TransactionAopAdministrationWrapper currentAopTransaction = DynamicallyTransactionManager.getCurrentAopTransaction();
                Assert.notNull(currentAopTransaction, "事务异常: 控制句柄为空" );
                DynamicallyTransactionManager.clearAopTransaction();
                for (String datasourceAlias : currentAopTransaction.getTransactionAffectDatasourceAlias()) {
                    SqlSession sqlSession = DynamicallyTransactionManager.getSqlSession(datasourceAlias);
                    if (sqlSession != null){
                        DynamicallySqlSessionUtil.closeSqlSession(datasourceAlias, sqlSession);
                        DynamicallyTransactionManager.removeSqlSession(datasourceAlias);
                    }
                }
                DynamicallyTransactionManager.clear();
            }
        }
        return result;
    }

    private TransactionAopAdministrationWrapper createWrapper(Method method){
        DynamicallySimpleRollBackTransactional annotation = AnnotationUtils.findAnnotation(method, DynamicallySimpleRollBackTransactional.class);
        if (annotation == null){
            annotation = AnnotationUtils.getAnnotation(method.getDeclaringClass(), DynamicallySimpleRollBackTransactional.class);
        }
        return new TransactionAopAdministrationWrapper(method, annotation.value());
    }
}
