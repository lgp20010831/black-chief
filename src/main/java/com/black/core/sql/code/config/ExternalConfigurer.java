package com.black.core.sql.code.config;

import com.black.core.sql.code.inter.*;
import com.black.core.sql.code.inter.ExecuteResultResolver;

import java.util.concurrent.LinkedBlockingQueue;

public interface ExternalConfigurer {

    default void blendSqlStatementCreator(LinkedBlockingQueue<DefaultSqlStatementCreator> creators){}

    default void blendSqlsArguramentResolver(LinkedBlockingQueue<SqlsArguramentResolver> resolvers){}

    default void blendAnnotationResolvers(LinkedBlockingQueue<ConfigurationAnnotationResolver> resolvers){}

    default void blendGroupHandlers(LinkedBlockingQueue<SqlValueGroupHandler> groupHandlers){}

    default void blendAppearanceResolvers(LinkedBlockingQueue<AppearanceResolver> appearanceResolvers){}

    default void blendResultResolvers(LinkedBlockingQueue<ExecuteResultResolver> resultResolvers){}

    default void blendSessionExecutor(LinkedBlockingQueue<PointSessionExecutor> sessionExecutors){}

    default void blendPrepareFinishResolvers(LinkedBlockingQueue<PrepareFinishResolver> prepareFinishResolvers){}

}
