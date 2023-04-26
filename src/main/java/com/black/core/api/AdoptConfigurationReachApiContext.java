package com.black.core.api;

import com.black.core.api.annotation.EnableApiCollector;
import com.black.core.api.handler.*;
import com.black.core.api.pojo.ApiController;
import com.black.core.api.tacitly.*;
import com.black.core.builder.Col;
import com.black.core.config.SpringAutoConfiguration;
import com.black.core.mvc.FileUtil;
import com.black.core.util.SimplePattern;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.AddHolder;
import com.black.core.spring.annotation.IgnorePrint;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.template.TxtTemplateHolder;
import lombok.extern.log4j.Log4j2;
import org.thymeleaf.context.Context;


import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@AddHolder
@LoadSort(60) @IgnorePrint
@LazyLoading(EnableApiCollector.class)
public class AdoptConfigurationReachApiContext implements ApiContext, OpenComponent, ApplicationDriver {

    public static final String API_KEY = "api";

    /** api 有效方法管理中心 */
    private ApiMethodManger apiMethodManger;

    /** api 扫描到别名 调度中心 */
    private ApiObjectDispatcher apiObjectDispatcher;

    /** api 依赖管理中心 */
    private ApiDependencyManger dependencyManger;

    /** 别名管理中心 */
    private ApiAliasManger aliasManger;

    /** 主要配置类 */
    private ApiHttpRestConfigurer configurer;

    /** 类扫描器 */
    private SimplePattern simplePattern;

    /** application 工厂 */
    private InstanceFactory instanceFactory;

    /** 持有模板工具的类 */
    private TxtTemplateHolder txtTemplateHolder;

    /** 主要解析类 **/
    private AnalysisProcessActuator analysisProcessActuator;

    /** 请求参数处理类 */
    private final Collection<RequestParamHandler>  requestParamHandlers = new ArrayList<>();

    /** 接口方法基本信息获取类 */
    private ApiInterfaceBaseHandler apiInterfaceBaseHandlers;

    /** 请求实例 reader */
    private final Collection<RequestExampleReader> requestExampleReaders = new ArrayList<>();

    /** 请求头处理类 */
    private final Collection<RequestHeadersHandler> requestHeadersHandlers = new ArrayList<>();

    /** 响应实例处理类 */
    private final Collection<ResponseExampleReader>  responseExampleReaders = new ArrayList<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        initAttribute(expansivelyApplication);
        txtTemplateHolder  = expansivelyApplication.queryComponent(TxtTemplateHolder.class);
        if (txtTemplateHolder == null){
            if (log.isWarnEnabled()) {
                log.warn("api auto builder need dependency txtTemplateHolder," +
                        " add @EnableTxtTemplateHolder join you application class");
            }
            return;
        }
        init(expansivelyApplication.getApplicationConfigurationMutes());
    }


    protected void init(Collection<?> configurations){
        List<?> configs = configurations.stream()
                .filter(c -> !c.getClass().equals(SpringAutoConfiguration.class) && c instanceof ApiHttpRestConfigurer)
                .collect(Collectors.toList());
        if (configs.isEmpty()){
            if (log.isDebugEnabled()) {
                log.debug("If the configuration class containing the configuration" +
                        " creation API in the project is not retrieved, the API document" +
                        " cannot be produced later");
            }
            return;
        }

        if (configs.size() > 1){
            if (log.isDebugEnabled()) {
                log.debug("The configuration file of API should be unique in the project," +
                        " otherwise it will lead to confusion,configs: {}", configs);
            }
            return;
        }

        configurer = (ApiHttpRestConfigurer) configs.get(0);
        readConfigAttribute();
    }

    protected void readConfigAttribute(){
        aliasManger = instanceFactory.getInstance(ApiAliasManger.class);
        instanceFactory.registerInstance(ApiHttpRestConfigurer.class, configurer);
        apiObjectDispatcher = instanceFactory.getInstance(ApiObjectDispatcher.class);

        //别名填充完毕
        apiObjectDispatcher.scannerObject();
        dependencyManger = instanceFactory.getInstance(ApiDependencyManger.class);

        //依赖关系整理完毕
        dependencyManger.refreshDependencyRelationship();

        //保存响应类
        ApiResponseHolder.apiResponseClass = configurer.registerResponseClass();

        apiMethodManger = instanceFactory.getInstance(ApiMethodManger.class);
        apiMethodManger.collectApiMethod();
        if (log.isDebugEnabled()) {
            log.debug("api 文档生成管理数据收集完毕");
        }
    }


    protected void initAttribute(ChiefExpansivelyApplication expansivelyApplication){
        instanceFactory = expansivelyApplication.instanceFactory();
        simplePattern = instanceFactory.getInstance(SimplePattern.class);
    }

    @Override
    public void writeApiDocs(String writeAbsolutelyPosition) {
        writeDocs(writeAbsolutelyPosition, aliasManger.getControllerAliasMap().values());
    }

    @Override
    public void writeApiDocs(String writeAbsolutelyPosition, String controllerPackage) {
        Set<Class<?>> loadClasses = simplePattern.loadClasses(controllerPackage);
        writeDocs(writeAbsolutelyPosition, loadClasses);
    }

    @Override
    public void writeApiDocs(String writeAbsolutelyPosition, Class<?>... pointClasses) {
        writeDocs(writeAbsolutelyPosition, Arrays.asList(pointClasses));
    }

    public void writeDocs(String writeAbsolutelyPosition, Collection<Class<?>> targetClasses){
        if (txtTemplateHolder != null){
            if (!txtTemplateHolder.isHasDependency()){
                throw new RuntimeException("txtTemplateHolder 不能正常使用, 检查是否是缺少依赖造成");
            }

            //初始化处理器队列
            initQueue();

            //检查文件路径
            checkPath(writeAbsolutelyPosition);

            //获取资源
            List<ApiController> source = getSource(targetClasses);

            //stream 数据
            final String stream = createStringStreamSource(source);

            //写入数据
            createFile(writeAbsolutelyPosition, stream);
            if (log.isInfoEnabled()) {
                log.info("create end");
            }
        }
    }

    protected void createFile(String path, String stream){
        File file = FileUtil.createFile(path);
        if (file == null){
            throw new RuntimeException("无法创建文件:" + stream);
        }
        FileUtil.writerFile(file, stream);
    }

    protected String createStringStreamSource(List<ApiController> apiControllers){
        Context context = txtTemplateHolder.createContext(Col.of(API_KEY, apiControllers));
        return txtTemplateHolder.getTemplateEngine().process(configurer.getThymeleafPath(), context);
    }

    protected void checkPath(String writeAbsolutelyPosition){
        if (writeAbsolutelyPosition == null){
            throw new RuntimeException("生成的文件路径不能为空");
        }

        if (!writeAbsolutelyPosition.endsWith(".md")){
            throw new RuntimeException("生成的文件必须是 md 类型");
        }
    }


    protected List<ApiController> getSource(Collection<Class<?>> targetClasses){

        //获取别名管理中心收集的所有别名
        Collection<Class<?>> values = aliasManger.getControllerAliasMap().values();

        //过滤调想要生成的 class 文档, 但是没有被别名中心收集到的 class
        List<Class<?>> afterFilterClassMutes = targetClasses
                .stream()
                .filter(values::contains)
                .collect(Collectors.toList());


        Collection<Class<?>> dependencyClassMutes = new HashSet<>();
        //获取控制器与实体类的依赖关系 map
        Map<Class<?>, List<Class<?>>> dependencyMap = dependencyManger.getDependencyMap();

        //整合所有依赖的实体类收集到一个集合中
        for (Class<?> filterClassMute : afterFilterClassMutes) {
            dependencyClassMutes.addAll(dependencyMap.get(filterClassMute));
        }

        //将实体类依赖进行去重
        List<Class<?>> pojoDependency = dependencyClassMutes.stream()
                .distinct()
                .collect(Collectors.toList());

        //最终结果
        return   afterFilterClassMutes.stream()
                .map(f -> analysisProcessActuator.invokeBuilderSource(f, pojoDependency))
                .collect(Collectors.toList());
    }

    protected void initQueue(){
        if (apiInterfaceBaseHandlers == null){
            apiInterfaceBaseHandlers = configurer.registerApiInterfaceBaseHandlers();
        }

        if (requestParamHandlers.isEmpty()){
            requestParamHandlers.add(instanceFactory.getInstance(TacitlyRequestParamHandler.class));
            configurer.addRequestParamHandlers(requestParamHandlers);
        }

        if (requestExampleReaders.isEmpty()){
            requestExampleReaders.add(instanceFactory.getInstance(TacitlyRequestExampleReader.class));
            configurer.addRequestExampleReader(requestExampleReaders);
        }

        if (requestHeadersHandlers.isEmpty()){
            requestHeadersHandlers.add(instanceFactory.getInstance(TacitlyRequestHeaderHandler.class));
            configurer.addRequestHeadersHandler(requestHeadersHandlers);
        }

        if (responseExampleReaders.isEmpty()){
            responseExampleReaders.add(instanceFactory.getInstance(TacitlyResponseReader.class));
            configurer.addResponseExampleReader(responseExampleReaders);
        }
        if (analysisProcessActuator == null){
            analysisProcessActuator = instanceFactory.getInstance(AnalysisProcessActuator.class);
            analysisProcessActuator.setRequestExampleReaders(requestExampleReaders);
            analysisProcessActuator.setApiInterfaceBaseHandlers(apiInterfaceBaseHandlers);
            analysisProcessActuator.setRequestHeadersHandlers(requestHeadersHandlers);
            analysisProcessActuator.setRequestParamHandlers(requestParamHandlers);
            analysisProcessActuator.setResponseExampleReaders(responseExampleReaders);
        }
    }
}
