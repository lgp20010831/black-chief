package com.black.core.mybatis.source;

import com.black.core.builder.Col;
import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.config.SpringAutoConfiguration;
import com.black.core.mybatis.source.annotation.EnableDynamicallyMultipleClients;
import com.black.core.mybatis.source.annotation.MultipleRepository;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.AddHolder;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.driver.PostPatternClazzDriver;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.util.StringUtils;
import com.black.holder.SpringHodler;
import com.black.utils.NameUtil;
import com.github.pagehelper.PageInterceptor;
import com.github.pagehelper.autoconfigure.PageHelperProperties;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@LoadSort(65)
@AddHolder
@LazyLoading(EnableDynamicallyMultipleClients.class)
public class IbatisDynamicallyMultipleDatabasesComponent implements OpenComponent,
        PostPatternClazzDriver, EnabledControlRisePotential, ApplicationDriver {

    private boolean debug;

    private final Map<String, DataSourceConnectWrapper> dataSources = new HashMap<>();

    private final Map<String, Configuration> configurationMap = new HashMap<>();

    private final Map<String, SqlSessionFactory> sqlSessionFactoryMap = new HashMap<>();

    private final Map<String, Object> mapperCache = new HashMap<>();

    private final Collection<Class<?>> earlyRepositoryCache = new HashSet<>();

    private ApplicationConfigurationReader reader;

    private final SqlSessionFactoryBuilder sqlSessionFactoryBuilder;

    private final Collection<Resource> mapperResources = new HashSet<>();

    private final Collection<String> typeAliasesPackages = new HashSet<>();

    private IbatisDataSourceGroupConfigurer configurer;

    private DefaultListableBeanFactory beanFactory;

    private AttributeParser attributeParser;

    private AnnotationSqlClientsHandler annotationSqlClientsHandler;

    private MybatisConfigurationHandler configurationHandler;

    private PageInterceptor pageInterceptor;

    private ChiefExpansivelyApplication application;

    private EnableDynamicallyMultipleClients enableDynamicallyMultipleClients;

    public IbatisDynamicallyMultipleDatabasesComponent() {
        sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        if (log.isInfoEnabled()) {
            log.info("register dynamically datasource:{}", sqlSessionFactoryMap.keySet());
        }
    }

    @Override
    public void postPatternClazz(Class<?> beanClazz, Map<Class<? extends OpenComponent>, Object> springLoadComponent, ReusingProxyFactory proxyFactory, ChiefExpansivelyApplication chiefExpansivelyApplication) {

        if (beanClazz.isInterface()){
            MultipleRepository repository = AnnotationUtils.getAnnotation(beanClazz, MultipleRepository.class);
            if (repository != null){
                earlyRepositoryCache.add(beanClazz);
            }
        }
    }


    @Override
    public void finallyEndPattern(Collection<Class<?>> clazzCollection,
                                  Map<Class<? extends OpenComponent>, Object> springLoadComponent,
                                  ReusingProxyFactory proxyFactory,
                                  ChiefExpansivelyApplication chiefExpansivelyApplication) {
        try {
            handlerConfig(chiefExpansivelyApplication.getApplicationConfigurationMutes());
            if (configurer == null){
                return;
            }
            for (Class<?> beanClass : earlyRepositoryCache) {
                MultipleRepository repository = AnnotationUtils.getAnnotation(beanClass, MultipleRepository.class);
                String alias = repository.value();
                if (configurationMap.containsKey(alias)){
                    try {
                        final IbatisComponentGiver ibatisComponentGiver = IbatisGiverManager.getIbatisComponentGiver();
                        Configuration configuration = configurationMap.get(alias);
                        if (!configuration.hasMapper(beanClass)){
                            configuration.addMapper(beanClass);
                        }
                        //create  SqlSessionFactory
                        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryMap
                                .computeIfAbsent(alias, k -> ibatisComponentGiver.getSqlSessionFactory(configuration));

                        //registerSqlSessionFactory(sqlSessionFactory, alias);
                        configurer.handlerSqlSessionFactory(alias, sqlSessionFactory);
                        handlerSqlSessionFactory(sqlSessionFactory, alias, beanClass, chiefExpansivelyApplication);
                        if (log.isDebugEnabled()) {
                            log.debug("successful register mapper bean to spring");
                        }

                    }catch (Throwable e){
                        CentralizedExceptionHandling.handlerException(e);
                        if (log.isErrorEnabled()) {
                            log.error("fail to handler mapper:{}", beanClass);
                        }
                    }
                }else {
                    if (log.isWarnEnabled()) {
                        log.warn("The configured mapper cannot find its dependent" +
                                " data source, and the alias does not exist： {}", alias);
                    }
                }
            }
        }catch (RuntimeException e){
            CentralizedExceptionHandling.handlerException(e);
            if (log.isErrorEnabled()) {
                log.error("handler config error");
            }
        }
    }


    private void handlerSqlSessionFactory(SqlSessionFactory factory, String alias,
                                          Class<?> beanClazz, ChiefExpansivelyApplication application){
        IbtaisMapperProxy mapperProxy = obtainMapperProxy(factory, alias);
        Object proxy = application.reusingProxy(beanClazz, mapperProxy);
        String beanName = NameUtil.getName(beanClazz);
        if (!beanFactory.containsBean(beanName)) {
            beanFactory.registerSingleton(beanName, proxy);
        }
        mapperCache.put(alias, proxy);
    }

    private IbtaisMapperProxy obtainMapperProxy(SqlSessionFactory sqlSessionFactory, String alias){
        return new IbtaisMapperProxy(sqlSessionFactory, configurer.obtainExecutorType(), alias, configurer);
    }


    private void registerSqlSessionFactory(SqlSessionFactory sqlSessionFactory, String alias){
        if (beanFactory != null){
            String beanName = StringUtils.linkStr(alias, "-SqlSessionFactory");
            if(!beanFactory.containsBean(beanName)){
                beanFactory.registerSingleton(beanName, sqlSessionFactory);
            }else {
                if (log.isWarnEnabled()) {
                    log.warn("SqlSessionFactory:{} is already exists", beanName);
                }
            }
        }
    }

    @Override
    public void postVerificationQualifiedDo(Annotation annotation, ChiefExpansivelyApplication application) {
        enableDynamicallyMultipleClients = (EnableDynamicallyMultipleClients) annotation;
        this.application = application;
        beanFactory = SpringHodler.getListableBeanFactory();
        reader = ApplicationConfigurationReaderHolder.getReader();
        attributeParser = new AttributeParser(reader);
        annotationSqlClientsHandler = new AnnotationSqlClientsHandler(application.getStartUpClazz(), attributeParser);
    }

    protected void handlerConfig(Collection<Object> configurationMutes){
        DataSourceRegister dataSourceRegister = obtainRegister();
        List<Object> configs = configurationMutes.stream()
                .filter(c -> !c.getClass().equals(SpringAutoConfiguration.class) && c instanceof IbatisDataSourceGroupConfigurer)
                .collect(Collectors.toList());

        if (!annotationSqlClientsHandler.canParse()){
            if (configs.isEmpty()){
                if (log.isWarnEnabled()) {
                    log.warn("Missing master profile, mapper It won't work");
                }
                return;
            }
        }

        if (configs.size() > 1){
            throw new RuntimeException("IbatisDataSourceGroupConfigurer size must be 1");
        }

        configurer = configs.isEmpty() ? new DefaultIbatisSourceGroupConfiguration() :
                (IbatisDataSourceGroupConfigurer) configs.get(0);
        debug = configurer.debug();
        configurer.registerDataSources(dataSourceRegister);
        if (annotationSqlClientsHandler.canParse()){
            dataSources.putAll(annotationSqlClientsHandler.parserTarget());
        }
        if (configurationHandler == null){
            configurationHandler = new MybatisConfigurationHandler(configurer, this);
        }


        parseSources();
        List<DataSourceRegister.SourceBuilder> sourceConnectWrappers = dataSourceRegister.getDataSourceConnectWrappers();
        Map<String, DataSourceConnectWrapper> beforeProcessorWrapper = new HashMap<>(dataSources);
        for (DataSourceRegister.SourceBuilder sourceBuilder : sourceConnectWrappers) {
            String alias = sourceBuilder.getAlias();
            if (dataSources.containsKey(alias)){
                throw new RuntimeException("重复的别名数据源:" + alias);
            }
            DataSourceConnectWrapper dataSource = obtainDataSource(alias);
            dataSource.driver(attributeParser.getReallyValue(sourceBuilder.getDriver()));
            dataSource.url(attributeParser.getReallyValue(sourceBuilder.getUrl()));
            dataSource.password(attributeParser.getReallyValue(sourceBuilder.getPassword()));
            dataSource.username(attributeParser.getReallyValue(sourceBuilder.getUsername()));
            beforeProcessorWrapper.put(alias, dataSource);
        }

        for (String alias : beforeProcessorWrapper.keySet()) {
            DataSourceConnectWrapper dataSource = beforeProcessorWrapper.get(alias);
            dataSource.setAutoCommit(configurer.setAutoCommit(alias));
            configurer.handlerDataSource(alias, dataSource.source());
            dataSources.put(alias, dataSource);
        }
        registerDataSources();
        createConfigurations();
    }

    private void registerDataSources(){
        dataSources.forEach((alias, data) ->{
            if (beanFactory != null){
                beanFactory.registerSingleton(DataSourceNameUtil.getDataSourceAlias(alias), data);
            }
        });
    }


    protected void addPageHelper(Configuration configuration){
        if (pageInterceptor == null){
            pageInterceptor = new PageInterceptor();
            Properties properties = new Properties();
            try {
                PageHelperProperties pageHelperProperties =
                        SpringHodler.getListableBeanFactory().getBean(PageHelperProperties.class);
                properties.putAll(pageHelperProperties.getProperties());
            }catch (BeansException e){
                //error for get pageHelperProperties
            }
            pageInterceptor.setProperties(properties);
        }
        configuration.addInterceptor(pageInterceptor);
    }


    protected void parseSources(){
        String[] locations = configurer.addMapperLocations();
        if (locations == null){
            locations = parseArray(reader.selectAttribute("mybatis.mapper-locations"));
        }
        if (locations == null){
            locations = new String[0];
        }
        Set<String> locationsSet = new HashSet<>(Col.as(locations));
        locationsSet.addAll(Col.as(enableDynamicallyMultipleClients.mapperLocations()));
        if(locationsSet != null){
            PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
            for (String location : locationsSet) {
                try {
                    mapperResources.addAll(Arrays.asList(patternResolver.getResources(location)));
                } catch (IOException e) {
                    CentralizedExceptionHandling.handlerException(e);
                    if (log.isWarnEnabled()) {
                        log.warn("handler mapper location error:{}", location);
                    }
                }
            }
        }
        String[] aliasesPackages = configurer.addTypeAliasesPackages();
        if (aliasesPackages != null){
            typeAliasesPackages.addAll(Arrays.asList(aliasesPackages));
        }
        typeAliasesPackages.addAll(Arrays.asList(enableDynamicallyMultipleClients.value()));
    }

    protected String[] parseArray(String value){
        if (value == null){
            return null;
        }
        return value.split(",");
    }

    protected void createConfigurations(){
        //获取 giver
        IbatisComponentGiver ibatisComponentGiver = IbatisGiverManager.getIbatisComponentGiver();
        for (String alias : dataSources.keySet()) {
            Configuration configuration = ibatisComponentGiver.getConfiguration(
                    createEnvironment(obtainTransactionFactory(), dataSources.get(alias).source()));
            configurationMap.put(alias, configuration);
            configurationHandler.parseConfig(configuration);
            if (debug){
                if (log.isInfoEnabled()) {
                    Map<String, Class<?>> typeAliases = configuration.getTypeAliasRegistry().getTypeAliases();
                    log.info("mapper: {}, typeAliases:{}", alias, typeAliases);
                    Collection<Class<?>> mappers = configuration.getMapperRegistry().getMappers();
                    log.info("mapper: {}, register mappers:{}", alias, mappers);
                }
            }
            addPageHelper(configuration);
            configurer.handlerConfiguration(alias, configuration);
        }
    }

    protected Environment createEnvironment(TransactionFactory transactionFactory, DataSource dataSource){
        return new Environment(UUID.randomUUID().toString(), transactionFactory, dataSource);
    }

    public EnableDynamicallyMultipleClients getEnableDynamicallyMultipleClients() {
        return enableDynamicallyMultipleClients;
    }

    public ChiefExpansivelyApplication getApplication() {
        return application;
    }

    protected TransactionFactory obtainTransactionFactory(){
        return new JdbcTransactionFactory();
    }

    protected DataSourceRegister obtainRegister(){
        return new DataSourceRegister();
    }

    protected DataSourceConnectWrapper obtainDataSource(String alias){
        return new DataSourceConnectWrapper(alias);
    }

    public Map<String, DataSourceConnectWrapper> getDataSources() {
        return dataSources;
    }

    public Map<String, Configuration> getConfigurationMap() {
        return configurationMap;
    }

    public Map<String, SqlSessionFactory> getSqlSessionFactoryMap() {
        return sqlSessionFactoryMap;
    }

    public Map<String, Object> getMapperCache() {
        return mapperCache;
    }

    public Collection<Class<?>> getEarlyRepositoryCache() {
        return earlyRepositoryCache;
    }

    public ApplicationConfigurationReader getYmlConfigurationHandler() {
        return reader;
    }

    public SqlSessionFactoryBuilder getSqlSessionFactoryBuilder() {
        return sqlSessionFactoryBuilder;
    }

    public Collection<Resource> getMapperResources() {
        return mapperResources;
    }

    public Collection<String> getTypeAliasesPackages() {
        return typeAliasesPackages;
    }

    public IbatisDataSourceGroupConfigurer getConfigurer() {
        return configurer;
    }
}
