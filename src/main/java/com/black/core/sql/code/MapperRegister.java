package com.black.core.sql.code;

import com.black.datasource.DataSourceBuilderTypeManager;
import com.black.datasource.MapSqlControllerElementResolver;
import com.black.datasource.ProduceElementDataSourceDiscriminateManager;

import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.factory.beans.xml.XmlBeanFactory;
import com.black.core.factory.beans.xml.XmlWrapper;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.ReflexUtils;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.sql.SQLSException;
import com.black.core.sql.annotation.*;
import com.black.core.sql.code.config.*;
import com.black.core.sql.code.datasource.DataSourceBuilderManager;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.node.*;
import com.black.core.sql.code.pattern.PipelinesManager;
import com.black.core.sql.code.sqls.GeneratedKeysResultSetHandler;
import com.black.core.sql.code.sqls.ResultSetThreadManager;
import com.black.core.sql.xml.XmlManager;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.MappingTypeConvert;
import com.black.core.util.StringUtils;
import com.black.vfs.SpringBootVfsLoader;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;


import java.io.IOException;
import java.util.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *  宗旨: 不需要实体类来进行数据库的操作
 *      优点: 代码量大大减少, 数据库字段变动时, 不要变动程序代码,
 *           并且可以随时刷新服务缓存的数据库结构, 实现不需要重启服务
 *      缺点: 对于中途参与项目的人不友好, 没有办法通过 swagger 生成文档
 *
 *      其实目前架构完全可以支持实体类,并且可以达到 jpa, mybatis 水平
 *
 *      mapperRegister 是一个启动类, 只需要实例化该类然后构造 mapper 就可以使用
 */
@SuppressWarnings("all") @Log4j2
public final class MapperRegister {

    private final Map<String, AnnotationMapperSQLApplicationContext> contextCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, AnnotationMapperSQLApplicationContext> mapperContextCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();

    private final InstanceFactory instanceFactory;

    private final BeanFactory beanFactory;

    private final GlobalDefaultReolverConfigurer defaultReolverConfigurer;

    private static MapperRegister register;

    private static Set<Class<?>> resolverQueue = new HashSet<>();

    public static boolean isTrip(){
        return register != null;
    }

    public static MapperRegister getInstance(){
        if (register == null){
            register = new MapperRegister();
        }
        return register;
    }

    MapperRegister() {
        defaultReolverConfigurer = new GlobalDefaultReolverConfigurer();
        FactoryManager.init();
        this.beanFactory = FactoryManager.getBeanFactory();
        this.instanceFactory = FactoryManager.getInstanceFactory();
        TypeHandler handler = TypeConvertCache.initAndGet();
        if (!handler.isParsed(MappingTypeConvert.class)) {
            handler.parse(Collections.singleton(new MappingTypeConvert()));
        }
        ProduceElementDataSourceDiscriminateManager.registerResolver(new MapSqlControllerElementResolver());
    }

    static {

        PipelinesManager.registerHook(pipeline -> {
            pipeline.addLast(new PointStatementLayer());
            pipeline.addLast(new SqlStatementLayer());
            pipeline.addLast(new ArguramentLayer());
            pipeline.addLast(new ConfigurationAnnotationValueLayer());
            pipeline.addLast(new StatementFinishLayer());
            pipeline.addLast(new SqlValueGroupLayer());
            pipeline.addLast(new AppearanceLayer());
            pipeline.addLast(new ResultLayer());
            pipeline.addLast(new SessionExecuteLayer());
        });

        ResultSetThreadManager.add(new GeneratedKeysResultSetHandler());
    }

    public static MapperRegister getRegister() {
        return getInstance();
    }

    public String getAlias(@NonNull Object mapper){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(mapper);
        if (!proxyCache.containsKey(primordialClass)){
            throw new IllegalStateException("该 mapper 并不存在此注册中心中");
        }
        return primordialClass.getAnnotation(GlobalConfiguration.class).value();
    }

    public void loadContext(){
        Collection<AnnotationMapperSQLApplicationContext> values = contextCache.values();
        for (AnnotationMapperSQLApplicationContext context : values) {
            context.loadDataSource();
        }
    }

    public <T> T getMapper(Class<T> mapperClass){
        return getMapper(mapperClass, null);
    }

    public <T> T getMapper(Class<T> mapperClass, Consumer<GlobalSQLConfiguration> configurationConsumer){
        if (proxyCache.containsKey(mapperClass)){
            return (T) proxyCache.get(mapperClass);
        }

        ClassWrapper<T> wrapper = ClassWrapper.get(mapperClass);
        if (!wrapper.getPrimordialClass().isInterface()) {
            throw new SQLSException("mapper must be an interface");
        }
        resolverQueue.add(mapperClass);
        try {
            AnnotationMapperSQLApplicationContext context = getContext(wrapper, configurationConsumer);
            GlobalSQLConfiguration globalSQLConfiguration = context.getConfiguration();
            T jdkProxy = context.createProxy(wrapper);
            bindMapper(wrapper, context);
            mapperContextCache.put(mapperClass, context);
            proxyCache.put(mapperClass, jdkProxy);
            return jdkProxy;
        }finally {
            resolverQueue.remove(mapperClass);
        }
    }

    public static void bindMapper(ClassWrapper<?> wrapper, AnnotationMapperSQLApplicationContext context){
        GlobalSQLConfiguration globalSQLConfiguration = context.getConfiguration();
        for (MethodWrapper mw : wrapper.getMethods()) {
            Supplier<Configuration> supplier = getConfigurationSupplier(mw, wrapper, globalSQLConfiguration);
            if (supplier != null){
                context.bind(mw, wrapper, supplier);
            }
        }
    }


    private AnnotationMapperSQLApplicationContext getContext(ClassWrapper<?> wrapper, Consumer<GlobalSQLConfiguration> configurationConsumer){
        GlobalConfiguration annotation = wrapper.getAnnotation(GlobalConfiguration.class);
        if (annotation == null){
            return parseImport(wrapper);
        }
        String alias = annotation.value();
        alias = StringUtils.hasText(alias) ? alias : BaseSQLApplicationContext.DEFAULT_ALIAS;
        String finalAlias = alias;
        return contextCache.computeIfAbsent(alias, as -> {
            if (log.isInfoEnabled()) {
                log.info("datasource:[{}] - registered multiple mappers ", finalAlias);
            }
            GlobalSQLConfiguration globalSQLConfiguration = new GlobalSQLConfiguration();
            AnnotationUtils.loadAttribute(annotation, globalSQLConfiguration);
            loadConfig(annotation, globalSQLConfiguration);
            globalSQLConfiguration.setDataSourceAlias(as);

            AnnotationMapperSQLApplicationContext applicationContext = createContext(globalSQLConfiguration, wrapper);
            log.info("alias: [{}], context type is [{}]", finalAlias, applicationContext.getClass().getSimpleName());
            if (configurationConsumer != null){
                configurationConsumer.accept(globalSQLConfiguration);
            }
            applicationContext.loadDataSource();
            return applicationContext;
        });
    }




    private AnnotationMapperSQLApplicationContext parseImport(ClassWrapper<?> wrapper){
        ImportMapper annotation = wrapper.getAnnotation(ImportMapper.class);
        ImportMapperAndPlatform mapperAndPlatform = wrapper.getAnnotation(ImportMapperAndPlatform.class);
        if (annotation == null && mapperAndPlatform == null){
            throw new IllegalStateException("mapper either configure information or import other mapper");
        }
        Class<?> mapperClass = annotation == null ? mapperAndPlatform.value() : annotation.value();
        if (resolverQueue.contains(mapperClass)){
            throw new IllegalStateException("[" + wrapper.getSimpleName() + "] imported mapper: [" + mapperClass.getSimpleName() + "] , " +
                    "processing queue, causing circular import");
        }

        getMapper(mapperClass);
        return mapperContextCache.get(mapperClass);
    }

    public void registerListener(GlobalSQLTectonicPeriodListener periodListener){
        getContextCache().forEach((alias, context) ->{
            context.registerGlobalSQLTectonicPeriodListener(periodListener);
        });
    }

    public void registerListener(GlobalSQLRunningListener runningListener){
        getContextCache().forEach((alias, context) ->{
            context.registerGlobalSQLRunningListener(runningListener);
        });
    }

    public static Supplier<Configuration> getConfigurationSupplier(MethodWrapper mw, ClassWrapper<?> wrapper, GlobalSQLConfiguration globalSQLConfiguration){
        Configurer annotation = mw.getAnnotation(Configurer.class);
        if (annotation == null){
            return null;
        }
        return new Supplier<Configuration>() {
            @Override
            public Configuration get() {
                Configuration configuration = new Configuration(globalSQLConfiguration, mw);
                configuration.setCw(wrapper);
                AnnotationUtils.loadAttribute(annotation, configuration);
                if (!StringUtils.hasText(configuration.getTableName())){
                    TableName tableName = wrapper.getAnnotation(TableName.class);
                    boolean dynamic = mw.getParameterByAnnotation(TableName.class) != null;
                    if (tableName == null && !dynamic){
                        throw new IllegalArgumentException("the mapper method needs to specify a table name " + mw.getMethod());
                    }
                    String name = tableName == null ? "" : tableName.value();
                    configuration.setTableName(name);
                }

                return wrapper.inlayAnnotation(ImportPlatform.class)
                        ? ConfigurationTreatment.treatmentConfig(configuration,
                        ClassWrapper.get(wrapper.getMergeAnnotation(ImportPlatform.class).value()))
                        : ConfigurationTreatment.treatmentConfig(configuration);
            }
        };
    }

    public static Configuration parseMethod(MethodWrapper mw, ClassWrapper<?> wrapper, GlobalSQLConfiguration globalSQLConfiguration){

        Configurer annotation = mw.getAnnotation(Configurer.class);
        if (annotation == null){
             return null;
        }
        Configuration configuration = new Configuration(globalSQLConfiguration, mw);
        configuration.setCw(wrapper);
        AnnotationUtils.loadAttribute(annotation, configuration);
        if (!StringUtils.hasText(configuration.getTableName())){
            TableName tableName = wrapper.getAnnotation(TableName.class);
            boolean dynamic = mw.getParameterByAnnotation(TableName.class) != null;
            if (tableName == null && !dynamic){
                throw new IllegalArgumentException("the mapper method needs to specify a table name " + mw.getMethod());
            }
            String name = tableName == null ? "" : tableName.value();
            configuration.setTableName(name);
        }

        return wrapper.inlayAnnotation(ImportPlatform.class)
                ? ConfigurationTreatment.treatmentConfig(configuration,
                ClassWrapper.get(wrapper.getMergeAnnotation(ImportPlatform.class).value()))
                : ConfigurationTreatment.treatmentConfig(configuration);
    }


    protected void loadConfig(GlobalConfiguration annotation, GlobalSQLConfiguration globalSQLConfiguration){
        String dataSourceAlias = globalSQLConfiguration.getDataSourceAlias();
        Class<? extends DataSourceBuilder> builder = annotation.builderClass();
        DataSourceBuilder dataSourceBuilder = DataSourceBuilderManager.obtain(dataSourceAlias, () -> {
            return DataSourceBuilderTypeManager.getBuilder(builder);
        });
        if (dataSourceBuilder instanceof SqlConfigurationAware){
            ((SqlConfigurationAware) dataSourceBuilder).setConfiguration(globalSQLConfiguration);
        }
        globalSQLConfiguration.setDataSourceBuilder(dataSourceBuilder);
        globalSQLConfiguration.setConvertHandler(ReflexUtils.instance(annotation.convertHandlerType()));
        Class<? extends Log> logImpl = annotation.logImpl();
        Log log = ReflexUtils.instance(logImpl);
        globalSQLConfiguration.setLog(log);

        List<ExternalConfigurer> configurers = new ArrayList<>();
        configurers.add(defaultReolverConfigurer);

        Class<? extends ExternalConfigurer> configurer = annotation.externalConfigurer();
        if (BeanUtil.isSolidClass(configurer)) {
            configurers.add(beanFactory.getSingleBean(configurer));
        }


        for (ExternalConfigurer externalConfigurer : configurers) {
            externalConfigurer.blendAnnotationResolvers(globalSQLConfiguration.getAnnotationResolvers());
            externalConfigurer.blendAppearanceResolvers(globalSQLConfiguration.getAppearanceResolvers());
            externalConfigurer.blendGroupHandlers(globalSQLConfiguration.getGroupHandlers());
            externalConfigurer.blendResultResolvers(globalSQLConfiguration.getResultResolvers());
            externalConfigurer.blendSqlsArguramentResolver(globalSQLConfiguration.getArguramentResolvers());
            externalConfigurer.blendSqlStatementCreator(globalSQLConfiguration.getCreators());
            externalConfigurer.blendSessionExecutor(globalSQLConfiguration.getSessionExecutors());
            externalConfigurer.blendPrepareFinishResolvers(globalSQLConfiguration.getPrepareFinishResolvers());
        }
    }

    protected AnnotationMapperSQLApplicationContext createContext(GlobalSQLConfiguration globalSQLConfiguration, ClassWrapper<?> wrapper){
        if (wrapper.hasAnnotation(MapperLocations.class)){
            if (XmlManager.isOpen()) {
                MapperXmlApplicationContext context = isOpenEntity(wrapper) ? new EntityXmlNapperApplicationContext(globalSQLConfiguration) :
                        new MapperXmlApplicationContext(globalSQLConfiguration);
                MapperLocations annotation = wrapper.getAnnotation(MapperLocations.class);
                String[] locations = annotation.value();
                if (!(beanFactory instanceof XmlBeanFactory)) {
                    throw new IllegalStateException("请适配 bean 工厂为 xml factory");
                }
                SpringBootVfsLoader bootVfsLoader = new SpringBootVfsLoader();
                ResourcePatternResolver resourceResolver = bootVfsLoader.getResourceResolver();
                for (String location : locations) {
                    try {
                        Resource[] resources = resourceResolver.getResources(location);
                        for (Resource resource : resources) {
                            log.info("find mapper resource: [{}]", resource.getFilename());
                            XmlWrapper xmlWrapper = (XmlWrapper) beanFactory.get(resource.getInputStream());
                            parseXmlWrapper(xmlWrapper, context);
                        }
                    } catch (IOException e) {
                        CentralizedExceptionHandling.handlerException(e);
                        log.error("can not find mapper xml resource: [{}]", location);
                    }
                }
                log.info("bind xml methods: {}", context.getCache().keySet());
                return context;
            }
            System.out.println("当前项目不支持 xml 解析, 请添加 dom4j 依赖, 重启再次尝试");
            System.out.println("<dependency>");
            System.out.println("    <groupId>dom4j</groupId>");
            System.out.println("    <artifactId>dom4j</artifactId>");
            System.out.println("    <version>1.6.1</version>");
            System.out.println("</dependency>");
        }
        return isOpenEntity(wrapper) ? new EntityNapperApplicationContext(globalSQLConfiguration) :
                new AnnotationMapperSQLApplicationContext(globalSQLConfiguration);
    }

    private void parseXmlWrapper(XmlWrapper xmlWrapper, MapperXmlApplicationContext context){
        ElementWrapper rootElement = xmlWrapper.getRootElement();
        if (!rootElement.getName().equals("mapper")) {
            throw new IllegalStateException("root element should is mapper, but is " + rootElement.getName());
        }
        List<ElementWrapper> qs = rootElement.getsByName("query");
        if(qs != null){
            for (ElementWrapper query : qs) {
                String id = query.getAttrVal("id");
                if (id == null){
                    throw new IllegalStateException("mapper element should label id");
                }
                if (context.save(id)) {
                    throw new IllegalStateException("Duplicate ID: " + id);
                }
                context.addBindMapper(id, query);
            }
        }

        List<ElementWrapper> us = rootElement.getsByName("update");
        if(us != null) {
            for (ElementWrapper query : us) {
                String id = query.getAttrVal("id");
                if (id == null){
                    throw new IllegalStateException("mapper element should label id");
                }
                if (context.save(id)) {
                    throw new IllegalStateException("Duplicate ID: " + id);
                }
                context.addBindMapper(id, query);
            }
        }

    }

    public Map<String, AnnotationMapperSQLApplicationContext> getContextCache() {
        return contextCache;
    }

    public void shutdown(){
        for (AnnotationMapperSQLApplicationContext context : contextCache.values()) {
            context.shutdown();
        }
    }

    private boolean isOpenEntity(ClassWrapper<?> mapperClass){
        return mapperClass.hasAnnotation(OpenEntity.class);
    }

    public Map<Class<?>, AnnotationMapperSQLApplicationContext> getMapperContextCache() {
        return mapperContextCache;
    }

    public Map<Class<?>, Object> getProxyCache() {
        return proxyCache;
    }

    @Override
    public String toString() {
        return "[" + hashCode() + "]register contexts:" + contextCache.keySet() + ";";
    }
}
