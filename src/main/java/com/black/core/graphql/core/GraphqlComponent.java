package com.black.core.graphql.core;

import com.black.core.chain.*;
import com.black.core.graphql.ann.EnabledGraphqlTransfer;
import com.black.core.graphql.ann.GraphqlResolver;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.tools.BeanUtil;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.graphql.GraphqlCrib;
import com.black.graphql.GraphqlHandler;
import com.black.graphql.annotation.GraphqlClient;
import com.black.utils.NameUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;

@Log4j2
@ChainClient(GraphqlComponent.class)
@LazyLoading(EnabledGraphqlTransfer.class)
public class GraphqlComponent implements OpenComponent, EnabledControlRisePotential, CollectedCilent, ChainPremise {

    private boolean factoryInstance;

    private Collection<Object> mapperTypeCache = new HashSet<>();

    private Collection<Object> handlerCache = new HashSet<>();

    private GraphqlCrib crib;

    final DefaultListableBeanFactory factory;

    public GraphqlComponent(DefaultListableBeanFactory factory) {
        this.factory = factory;
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        crib = new GraphqlCrib(factoryInstance);
        for (Object h : handlerCache) {
            crib.registerHandler((GraphqlHandler) h);
        }

        for (Object o : mapperTypeCache) {
            Object mapper = crib.getMapper((Class<?>) o);
            String name = NameUtil.getName(mapper);
            try {

                log.info("register graphql mapper: [{}]", name);
                factory.registerSingleton(name, mapper);
            }catch (BeansException e){
                CentralizedExceptionHandling.handlerException(e);
                log.error("fail to register single mapper: [{}]", name);
            }
        }
    }


    @Override
    public void postVerificationQualifiedDo(Annotation annotation, ChiefExpansivelyApplication application) {
        EnabledGraphqlTransfer graphqlTransfer = (EnabledGraphqlTransfer) annotation;
        factoryInstance = graphqlTransfer.factoryInstance();
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry mapper = register.begin("mapper", juh -> {
            return juh.isInterface() && juh.isAnnotationPresent(GraphqlClient.class);
        });
        mapper.instance(false);
        register.begin("handler", ghj -> {
            return GraphqlHandler.class.isAssignableFrom(ghj) &&
                    BeanUtil.isSolidClass(ghj) &&
                    ghj.isAnnotationPresent(GraphqlResolver.class);
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if (resultBody.getAlias().equals("mapper")) {
            mapperTypeCache.addAll(resultBody.getCollectSource());
        }

        if (resultBody.getAlias().equals("handler")) {
            handlerCache.addAll(resultBody.getCollectSource());
        }
    }

    @Override
    public boolean premise() {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        if (mainClass != null){
            return mainClass.isAnnotationPresent(EnabledGraphqlTransfer.class);
        }
        return false;
    }
}
