package com.black.core.sql.code.config;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.sql.code.inter.*;
import com.black.core.tools.BeanUtil;
import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Log4j2
public class GlobalDefaultReolverConfigurer implements ExternalConfigurer{

    final BeanFactory beanFactory;

    final VfsScanner vfsScanner;

    final Map<String, Set<Class<?>>> cache = new ConcurrentHashMap<>();

    public GlobalDefaultReolverConfigurer(){
        FactoryManager.init();
        beanFactory = FactoryManager.getBeanFactory();
        vfsScanner = VFS.findVfsScanner();
    }



    @Override
    public void blendSqlStatementCreator(LinkedBlockingQueue<DefaultSqlStatementCreator> creators) {
        Set<Class<?>> typeSet = cache.computeIfAbsent("com.black.core.sql.code.impl.statement_impl", vfsScanner::load);
        for (Class<?> type : typeSet) {
            if (BeanUtil.isSolidClass(type)){
                creators.add((DefaultSqlStatementCreator) beanFactory.getSingleBean(type));
            }
        }
        log.info("load default creator: [{}]", creators.size());
    }

    @Override
    public void blendSqlsArguramentResolver(LinkedBlockingQueue<SqlsArguramentResolver> resolvers) {
        Set<Class<?>> typeSet = cache.computeIfAbsent("com.black.core.sql.code.impl.sqlarg_impl", vfsScanner::load);
        for (Class<?> type : typeSet) {
            if (BeanUtil.isSolidClass(type)){
                try {
                    resolvers.add((SqlsArguramentResolver) beanFactory.getSingleBean(type));
                }catch (Throwable e){}
            }
        }
        log.info("load default sqlArguramentResolvers: [{}]", resolvers.size());
    }

    @Override
    public void blendAnnotationResolvers(LinkedBlockingQueue<ConfigurationAnnotationResolver> resolvers) {
        Set<Class<?>> typeSet = cache.computeIfAbsent("com.black.core.sql.code.impl.config_impl", vfsScanner::load);
        for (Class<?> type : typeSet) {
            if (BeanUtil.isSolidClass(type)){
                resolvers.add((ConfigurationAnnotationResolver) beanFactory.getSingleBean(type));
            }
        }
        log.info("load default ConfigurationAnnotationResolver: [{}]", resolvers.size());
    }

    @Override
    public void blendGroupHandlers(LinkedBlockingQueue<SqlValueGroupHandler> groupHandlers) {
        Set<Class<?>> typeSet = cache.computeIfAbsent("com.black.core.sql.code.impl.sqlvalue_impl", vfsScanner::load);
        for (Class<?> type : typeSet) {
            if (BeanUtil.isSolidClass(type)){
                groupHandlers.add((SqlValueGroupHandler) beanFactory.getSingleBean(type));
            }
        }
        log.info("load default groupHandlers: [{}]", groupHandlers.size());
    }

    @Override
    public void blendAppearanceResolvers(LinkedBlockingQueue<AppearanceResolver> appearanceResolvers) {
        Set<Class<?>> typeSet = cache.computeIfAbsent("com.black.core.sql.code.impl.appearance_impl", vfsScanner::load);
        for (Class<?> type : typeSet) {
            if (BeanUtil.isSolidClass(type)){
                appearanceResolvers.add((AppearanceResolver) beanFactory.getSingleBean(type));
            }
        }
        log.info("load default appearanceResolvers: [{}]", appearanceResolvers.size());
    }

    @Override
    public void blendResultResolvers(LinkedBlockingQueue<ExecuteResultResolver> resultResolvers) {
        Set<Class<?>> typeSet = cache.computeIfAbsent("com.black.core.sql.code.impl.result_impl", vfsScanner::load);
        for (Class<?> type : typeSet) {
            if (BeanUtil.isSolidClass(type)){
                resultResolvers.add((ExecuteResultResolver) beanFactory.getSingleBean(type));
            }
        }
        log.info("load default resultResolvers: [{}]", resultResolvers.size());
    }

    @Override
    public void blendSessionExecutor(LinkedBlockingQueue<PointSessionExecutor> sessionExecutors) {
        Set<Class<?>> typeSet = cache.computeIfAbsent("com.black.core.sql.code.impl.seesion_impl", vfsScanner::load);
        for (Class<?> type : typeSet) {
            if (BeanUtil.isSolidClass(type)){
                sessionExecutors.add((PointSessionExecutor) beanFactory.getSingleBean(type));
            }
        }
        log.info("load default sessiontResolvers: [{}]", sessionExecutors.size());
    }


    @Override
    public void blendPrepareFinishResolvers(LinkedBlockingQueue<PrepareFinishResolver> prepareFinishResolvers) {
        Set<Class<?>> typeSet = cache.computeIfAbsent("com.black.core.sql.code.impl.prepare_impl", vfsScanner::load);
        for (Class<?> type : typeSet) {
            if (BeanUtil.isSolidClass(type)){
                prepareFinishResolvers.add((PrepareFinishResolver) beanFactory.getSingleBean(type));
            }
        }
        log.info("load default prepareResolvers: [{}]", prepareFinishResolvers.size());
    }

}
