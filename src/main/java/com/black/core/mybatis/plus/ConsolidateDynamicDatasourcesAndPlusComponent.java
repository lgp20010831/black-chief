package com.black.core.mybatis.plus;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.override.MybatisMapperMethod;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.black.holder.SpringHodler;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.mybatis.source.IbatisComponentGiver;
import com.black.core.mybatis.source.IbatisGiverManager;
import com.black.core.mybatis.source.MapperMethodWrapper;
import com.black.core.mybatis.source.annotation.EnableDynamicallyMultipleClients;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.IgnorePrint;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.PostPatternClazzDriver;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

@Log4j2
@LoadSort(45)
@IgnorePrint
@LazyLoading(EnableAutoAdaptationDynamicllyIbaisPlus.class)
public class ConsolidateDynamicDatasourcesAndPlusComponent implements OpenComponent, EnabledControlRisePotential, PostPatternClazzDriver {

    /**
     * plus原理
     * mapperScanner 将制定包下的所有接口
     * 并封装成 FactoryBean
     * plusAutoConfiguration 替换掉了mybatis原有的 sqlSessionFactory 和 configuraion
     * 如果使用{@link com.black.core.aop.ibatis.DynamicallyTransactionClient}
     * 并且目标 mapper 也在扫描包范围下, 就会被替换成 factoryBean, 导致跨数据源失败
     * 如果不在那个包底下, 则无法被 plus 解析也就没有那些增强功能
     *
     *
     * 整合的步骤
     * 将创建的 ibatisProxy 里的 configuration 重新加载, 替换成 plus 的configuration
     * MapperMethod 也替换成 plus 的
     * 就是在原有动态数据源逻辑不变的情况下, 实现替换
     * 最简单方法, 直接将 IbatisDynamicallyMultipleDatabasesComponent 复制一份
     * 然后所有关于 mybatis 的换成 plus 也能实现
     */
    @Override
    public void postVerificationQualifiedDo(Annotation annotation, ChiefExpansivelyApplication application) {
        Class<?> startUpClazz = application.getStartUpClazz();
        EnableDynamicallyMultipleClients client = AnnotationUtils.findAnnotation(startUpClazz, EnableDynamicallyMultipleClients.class);
        if (client == null){
            return;
        }

        try {
            Class.forName("com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean");
        } catch (ClassNotFoundException e) {
            return;
        }

        DefaultListableBeanFactory beanFactory = SpringHodler.getListableBeanFactory();
        MybatisPlusProperties mybatisProperties = beanFactory.getBean(MybatisPlusProperties.class);
        //注册
        IbatisGiverManager.registerGiver(new PlusIbatisComponentGiver(mybatisProperties));
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {

    }

    public static class PlusIbatisComponentGiver implements IbatisComponentGiver{

        GlobalConfig globalConfig = GlobalConfigUtils.defaults();
        final MybatisPlusProperties properties;

        public PlusIbatisComponentGiver(MybatisPlusProperties mybatisProperties) {
            this.properties = mybatisProperties;
        }

        @Override
        public SqlSessionFactory getSqlSessionFactory(Configuration configuration) {
            MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
            factory.setDataSource(configuration.getEnvironment().getDataSource());
            factory.setVfs(SpringBootVFS.class);
            factory.setConfiguration((MybatisConfiguration) configuration);

//            if (StringUtils.hasText(this.properties.getConfigLocation())) {
//                factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
//            }
//            applyConfiguration(factory);
            if (this.properties.getConfigurationProperties() != null) {
                factory.setConfigurationProperties(this.properties.getConfigurationProperties());
            }
//            if (!ObjectUtils.isEmpty(this.interceptors)) {
//                factory.setPlugins(this.interceptors);
//            }
            if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
                factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
            }
            if (this.properties.getTypeAliasesSuperType() != null) {
                factory.setTypeAliasesSuperType(this.properties.getTypeAliasesSuperType());
            }
            if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
                factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
            }
            Resource[] mapperLocations = this.properties.resolveMapperLocations();
            if (!ObjectUtils.isEmpty(mapperLocations)) {
                factory.setMapperLocations(mapperLocations);
            }
            // TODO 修改源码支持定义 TransactionFactory
            //this.getBeanThen(TransactionFactory.class, factory::setTransactionFactory);

            // TODO 对源码做了一定的修改(因为源码适配了老旧的mybatis版本,但我们不需要适配)
            Class<? extends LanguageDriver> defaultLanguageDriver = this.properties.getDefaultScriptingLanguageDriver();
            Optional.ofNullable(defaultLanguageDriver).ifPresent(factory::setDefaultScriptingLanguageDriver);

            // TODO 注入填充器
            //this.getBeanThen(MetaObjectHandler.class, globalConfig::setMetaObjectHandler);
            // TODO 注入主键生成器
            //this.getBeansThen(IKeyGenerator.class, i -> globalConfig.getDbConfig().setKeyGenerators(i));
            // TODO 注入sql注入器
            //this.getBeanThen(ISqlInjector.class, globalConfig::setSqlInjector);
            // TODO 注入ID生成器
            //his.getBeanThen(IdentifierGenerator.class, globalConfig::setIdentifierGenerator);
            // TODO 设置 GlobalConfig 到 MybatisSqlSessionFactoryBean
            globalConfig.setBanner(false);
            factory.setGlobalConfig(globalConfig);

            SqlSessionFactory sqlSessionFactory = null;
            try {
                 sqlSessionFactory = factory.getObject();
                 globalConfig.setSqlSessionFactory(sqlSessionFactory);
            } catch (Exception e) {
                CentralizedExceptionHandling.handlerException(e);
                if (log.isErrorEnabled()) {
                    log.error("适配 plus 异常");
                }
            }
            return sqlSessionFactory;
        }

        @Override
        public Configuration getConfiguration(Environment environment) {
            MybatisConfiguration mybatisConfiguration = new MybatisConfiguration(environment);
            String attribute = ApplicationConfigurationReaderHolder.getReader().selectAttribute("mybatis-plus.configuration.log-impl");
            if (attribute != null){
                try {
                    mybatisConfiguration.setLogImpl((Class<? extends Log>) Class.forName(attribute));
                } catch (ClassNotFoundException e) {
                    mybatisConfiguration.setLogImpl(StdOutImpl.class);
                }
            }
//
            mybatisConfiguration.setVfsImpl(SpringBootVFS.class);
            return mybatisConfiguration;
        }

        @Override
        public MapperMethodWrapper createMapperMethod(Class<?> interfaceClass, Method method, Configuration configuration) {
            return new PlusMapperMethodWrapper(interfaceClass, method, configuration);
        }
    }

    public static class PlusMapperMethodWrapper implements MapperMethodWrapper{

        final MybatisMapperMethod mybatisMapperMethod;

        public PlusMapperMethodWrapper(Class<?> interfaceClass, Method method, Configuration configuration) {
            mybatisMapperMethod = new MybatisMapperMethod(interfaceClass, method, configuration);
        }

        @Override
        public Object execute(SqlSession sqlSession, Object[] args) {
            return mybatisMapperMethod.execute(sqlSession, args);
        }
    }


}
