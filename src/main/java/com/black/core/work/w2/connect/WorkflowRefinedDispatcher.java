package com.black.core.work.w2.connect;

import com.alibaba.fastjson.JSONObject;
import com.black.core.chain.*;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.ClosableSort;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.tools.BeanUtil;
import com.black.core.work.w1.WorkFlowSchedulerCache;
import com.black.core.work.w2.WorkflowTypeHandler;
import com.black.core.work.w2.connect.annotation.EnableWorkflowRefinedModule;
import com.black.core.work.w2.connect.annotation.WorkflowRunnableInvoker;
import com.black.core.work.w2.connect.annotation.WriedWorkflowNodes;
import com.black.core.work.w2.connect.annotation.WriedWorkflowRouteResolver;
import com.black.core.work.w2.connect.builder.NodeBuilder;
import com.black.core.work.w2.connect.builder.NodeBuilderAuthentication;
import com.black.core.work.w2.connect.builder.NodeBuilderLeader;
import com.black.core.work.w2.connect.builder.NodeBuilderManager;
import com.black.core.work.w2.connect.cache.SqlServiceWriter;
import com.black.core.work.w2.connect.cache.WorkflowDatabaseCache;
import com.black.core.work.w2.connect.check.WorkflowBalance;
import com.black.core.work.w2.connect.config.AnnotationConfigurationProcessor;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.entry.WorkflowEntry;
import com.black.core.work.w2.connect.entry.WorkflowNodeInstanceEntry;
import com.black.core.work.w2.connect.entry.WorkflowNodeModuleEntry;
import com.black.core.work.w2.connect.entry.WorkflowRouteEntry;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;
import com.black.core.work.w2.connect.runnable.WorkflowNodeRunnableHandler;
import com.black.core.work.w2.connect.time.DefaultScheduledServiceDispatcher;
import com.black.core.work.w1.time.ScheduledFuturePoolManager;
import com.black.core.work.w2.connect.tracker.TrackerIdentifying;
import com.black.core.work.w2.connect.tracker.WorkflowEventTracker;
import com.black.core.work.w2.service.DefaultWorkflowServiceImpl;
import com.black.core.work.w2.service.ServiceHolder;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@LoadSort(8957)
@ClosableSort(250)
@Log4j2  @ChainClient(WorkflowPremise.class)
@LazyLoading(EnableWorkflowRefinedModule.class)
public class WorkflowRefinedDispatcher implements OpenComponent, CollectedCilent,
        EnabledControlRisePotential, ApplicationDriver {

    private InstanceFactory instanceFactory;
    public static final String RESOLVER_ALIAS = "resolver";
    public static final String DEFINITION_ALIAS = "definition";
    public static final String NODES_ALIAS = "nodes";
    public static final String HANDLER_ALIAS = "handlers";
    public static final String NODE_BUILDERS = "builders";
    public static final String TRACKER_ALIAS = "tracker";
    protected Collection<WorkflowProcessing> systemEngines = new HashSet<>();
    private final Map<String, WorkflowNodeDefinitional> nodeDefinitionals = new HashMap<>();
    private final Map<String, WorkflowDefinitional> workflowDefinitionalCache = new HashMap<>();
    private WorkflowConfiguration workflowConfiguration;
    private AnnotationConfigurationProcessor configurationProcessor;
    private final GlobalWorkflowManagementCenter workflowManagementCenter;
    private NodeBuilderManager builderManager;
    private Collection<Object> collectSource;
    private boolean collectNode = false;
    private boolean collectWorkflow = false, collectBuilder = false;

    public WorkflowRefinedDispatcher(){
        WorkflowRefinedManager.dispatcher = this;
        workflowManagementCenter = GlobalWorkflowManagementCenter.getInstance();
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {

        WorkflowTypeHandler.load();

        //初始化缓存工具
        workflowConfiguration.setWriteDatabaseHandler(instanceFactory.getInstance(SqlServiceWriter.class));

        //接下来要校准程序里和数据库里的模板
        calibration();

        init();
    }

    protected void init(){
        //初始化, 读取数据库里目前正在运行的实例对象
        //重新部署到任务中
        for (WorkflowDefinitional definitional : workflowDefinitionalCache.values()) {
            WorkflowProcessing engine = definitional.getEngine();
            engine.init(definitional, this);
        }
    }

    @Override
    public void whenApplicationStop(ChiefExpansivelyApplication application) {
        for (WorkflowDefinitional definitional : workflowDefinitionalCache.values()) {
            WorkflowProcessing processing = definitional.getEngine();
            processing.close();
        }
        ApplicationDriver.super.whenApplicationStop(application);
    }

    @Override
    public void postVerificationQualifiedDo(Annotation annotation, ChiefExpansivelyApplication application) {
        WorkFlowSchedulerCache.setWorkflowRefinedDispatcher(this);
        instanceFactory = application.instanceFactory();
        configurationProcessor = new AnnotationConfigurationProcessor((EnableWorkflowRefinedModule) annotation, instanceFactory);

        //创建全局配置类
        workflowConfiguration = configurationProcessor.handler();
        workflowConfiguration.setDispatcher(this);
        workflowManagementCenter.setWorkflowConfiguration(workflowConfiguration);
        ScheduledFuturePoolManager.poolSize = workflowConfiguration.getCorePoolSize();
        workflowConfiguration.setScheduledTaskDispatcher(new DefaultScheduledServiceDispatcher(ScheduledFuturePoolManager.getScheduledService()));

        //构造  service
        ServiceHolder.setService(new DefaultWorkflowServiceImpl(workflowConfiguration));

        //通过 factory 实例化条件处理器
        ConditionResolver conditionResolver = instanceFactory.getInstance(((EnableWorkflowRefinedModule) annotation).conditionResolverType());
    }


    protected void calibration(){

        //获取验证类
        WorkflowBalance balance = workflowConfiguration.getWriteDatabaseHandler().getBalance();
        try {

            //获取数据库里所有的节点
            Map<String, WorkflowNodeModuleEntry> checkNodeCache = balance.getNodeModuleEntryCache();

            //校验节点
            nodeDefinitionals.forEach((alias, definitional) ->{
                String id;
                if (!checkNodeCache.containsKey(alias)){
                    //新增 node
                    id = workflowConfiguration.getWriteDatabaseHandler().writeNodeModule(definitional);
                    definitional.setId(id);
                }else {

                    //如果已经存在则, 更新节点定义
                    definitional.setId(checkNodeCache.get(alias).getId());
                    workflowConfiguration.getWriteDatabaseHandler().updateNodeModule(definitional);
                }

            });

            //刷新 node module 数据, 保持最新
            balance.refrushNodeModule();

            //获取数据库里的所有工作流模板
            Map<String, WorkflowEntry> databaseEntries = balance.getWorkflowEntryCache();
            workflowDefinitionalCache.forEach((alias, definition) ->{

                if (databaseEntries.containsKey(alias)){
                    definition.setId(databaseEntries.get(alias).getId());
                    workflowConfiguration.getWriteDatabaseHandler().writeEngine(definition, true);
                }else {

                    //将新模板加入数据库中
                    definition.setId(workflowConfiguration.getWriteDatabaseHandler().writeEngine(definition, false));
                }
            });

            balance.refushWorkflow();
            balance.refrushNodeInstance();
            balance.refrushRoute();

            //获取数据库里所有的路由信息
            Map<String, WorkflowRouteEntry> databaseRoutes = balance.getNodeRouteCache();
            Map<String, WorkflowNodeInstanceEntry> nodeInstanceEntryCache = balance.getNodeInstanceEntryCache();
            for (WorkflowDefinitional definitional : workflowDefinitionalCache.values()) {

                for (ConnectRouteWraper routeWraper : definitional.getRouteWrapers()) {
                    String id;
                    String routeAlias = routeWraper.getName();
                    if (!databaseRoutes.containsKey(routeAlias)){
                        //添加路由节点
                        id = workflowConfiguration.getWriteDatabaseHandler().writeRoute(routeWraper, definitional);
                    }else {
                        WorkflowRouteEntry workflowRouteEntry = databaseRoutes.get(routeAlias);
                        routeWraper.setConditionExpression(workflowRouteEntry.getConditional());
                        id = workflowRouteEntry.getId();
                    }
                    routeWraper.setId(id);
                }

                for (WorkflowNode workflowNode : definitional.getNodeAliasSet()) {
                    String id;
                    String name = workflowNode.name();
                    if (nodeInstanceEntryCache.containsKey(name)){
                        id = nodeInstanceEntryCache.get(name).getId();
                    }else {
                        id = workflowConfiguration.getWriteDatabaseHandler().writeNode(workflowNode, definitional);
                    }
                    workflowNode.setId(id);
                }
            }


        }finally {
            balance.clear();
            ((WorkflowDatabaseCache)workflowConfiguration.getWriteDatabaseHandler()).refrush();
        }
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin(DEFINITION_ALIAS, rc -> WorkflowAdaptation.class.isAssignableFrom(rc) &&
                BeanUtil.isSolidClass(rc) && AnnotationUtils.getAnnotation(rc, WorkflowDefinition.class) != null);

        ConditionEntry handlers = register.begin(HANDLER_ALIAS, rc -> WorkflowNodeRunnableHandler.class.isAssignableFrom(rc) &&
                BeanUtil.isSolidClass(rc) && AnnotationUtils.getAnnotation(rc, WorkflowRunnableInvoker.class) != null);

        ConditionEntry conditionEntry = register.begin(RESOLVER_ALIAS, rc -> WorkflowRouteResolver.class.isAssignableFrom(rc) &&
                BeanUtil.isSolidClass(rc) && AnnotationUtils.getAnnotation(rc, WriedWorkflowRouteResolver.class) != null);

        ConditionEntry nodeGiver = register.begin(NODES_ALIAS, ng -> WorkflowNodeDefinitionalAdapation.class.isAssignableFrom(ng) &&
                BeanUtil.isSolidClass(ng) && AnnotationUtils.getAnnotation(ng, WriedWorkflowNodes.class) != null);

        ConditionEntry builderGiver = register.begin(NODE_BUILDERS, gh -> NodeBuilder.class.isAssignableFrom(gh) &&
                BeanUtil.isSolidClass(gh) && AnnotationUtils.getAnnotation(gh, NodeBuilderAuthentication.class) != null);
        builderGiver.instance(false);

        register.begin(TRACKER_ALIAS, gh -> WorkflowEventTracker.class.isAssignableFrom(gh) &&
                BeanUtil.isSolidClass(gh) && AnnotationUtils.getAnnotation(gh, TrackerIdentifying.class) != null);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {

        if (resultBody.getAlias().equals(DEFINITION_ALIAS)) {
            collectSource = resultBody.getCollectSource();
        }

        if (resultBody.getAlias().equals(RESOLVER_ALIAS)){
            workflowConfiguration.setRouteResolvers(resultBody.getCollectSource()
                    .stream().map(r -> (WorkflowRouteResolver)r)
                    .collect(Collectors.toList()));
        }

        if (resultBody.getAlias().equals(HANDLER_ALIAS)){
            workflowConfiguration.setRunnableHandlers(resultBody.getCollectSource()
                    .stream().map(r -> (WorkflowNodeRunnableHandler)r)
                    .collect(Collectors.toSet()));
        }

        if (resultBody.getAlias().equals(TRACKER_ALIAS)){
            for (Object obj : resultBody.getCollectSource()) {
                Map<String, Collection<WorkflowEventTracker>> trackers = workflowConfiguration.getTrackers();
                TrackerIdentifying identifying = AnnotationUtils.getAnnotation(BeanUtil.getPrimordialClass(obj), TrackerIdentifying.class);
                if (identifying != null){
                    for (String name : identifying.value()) {
                        Collection<WorkflowEventTracker> collection = trackers.computeIfAbsent(name, k -> new HashSet<>());
                        collection.add((WorkflowEventTracker) obj);
                    }
                }
            }
        }

        if (resultBody.getAlias().equals(NODE_BUILDERS)){
            workflowConfiguration.setBuilderTypes(resultBody.getCollectSource()
                    .stream()
                    .map(c -> (Class<? extends NodeBuilder>)c)
                    .collect(Collectors.toSet()));
            collectBuilder = true;
        }

        if (resultBody.getAlias().equals(NODES_ALIAS)){
            collectNode = true;
            for (Object obj : resultBody.getCollectSource()) {
                WorkflowNodeDefinitionalAdapation nodeAdapation = (WorkflowNodeDefinitionalAdapation) obj;
                for (WorkflowNodeDefinitional nodeDefinitional : nodeAdapation.getNodes()) {
                    String name = nodeDefinitional.name();
                    if (name == null){
                        throw new RuntimeException("节点别名一定不能为 null");
                    }

                    if (nodeDefinitionals.containsKey(name)){
                        throw new RuntimeException("node name: " + name + " is aleary existence");
                    }
                    nodeDefinitionals.put(name, nodeDefinitional);
                }
            }
        }

        if (collectNode && collectBuilder && collectSource != null && !collectWorkflow){
            collectWorkflow = true;

            //开始收集工作流模板
            for (Object obj : collectSource) {
                WorkflowDefinition workflowDefinition = AnnotationUtils.getAnnotation(BeanUtil.getPrimordialClass(obj), WorkflowDefinition.class);
                String alias = workflowDefinition.value();
                if (workflowDefinitionalCache.containsKey(alias)){
                    throw new RuntimeException("workflow alias: " + alias + " has aleary existence");
                }
                if (builderManager == null){
                    builderManager = new NodeBuilderManager(workflowConfiguration);
                }
                //WorkflowBuilder workflowBuilder = workflowManagementCenter.getWorkflowBuilder(workflowDefinition.hang() ? WorkflowStatus.HANG : WorkflowStatus.ACTIVATION, alias);
                NodeBuilderLeader leader = builderManager.getLeader(alias, workflowDefinition.hang() ? WorkflowStatus.HANG : WorkflowStatus.ACTIVATION);
                WorkflowAdaptation adaptation = (WorkflowAdaptation) obj;
                WorkflowDefinitional definitional = adaptation.getEngine(leader);
                workflowDefinitionalCache.put(alias, definitional);
                workflowManagementCenter.callBack(definitional);

                //创建模板
                WorkflowProcessing processing = ProcessingFactory.createProcessing(definitional, this);

                //将模板加入缓存
                systemEngines.add(processing);
                definitional.setEngine(processing);
            }
        }
    }

    public WorkflowConfiguration getWorkflowConfiguration() {
        return workflowConfiguration;
    }

    public Map<String, WorkflowNodeDefinitional> getNodeDefinitionals() {
        return nodeDefinitionals;
    }

    public Map<String, WorkflowDefinitional> getWorkflowDefinitionalCache() {
        return workflowDefinitionalCache;
    }

    public WorkflowProcessing queryProcessingById(String id){
        for (WorkflowDefinitional definitional : workflowDefinitionalCache.values()) {
            if (id.equals(definitional.getId())) {
                return definitional.getEngine();
            }
        }
        return null;
    }

    public void parkWorkflow(String name){
        WorkflowDefinitional definitional = workflowDefinitionalCache.get(name);
        if (definitional != null){
            definitional.setStatus(WorkflowStatus.HANG);
        }
        workflowConfiguration.getWriteDatabaseHandler().parkWorkflow(name);
    }

    public void activitiWorkflow(String name){
        WorkflowDefinitional definitional = workflowDefinitionalCache.get(name);
        if (definitional != null){
            definitional.setStatus(WorkflowStatus.ACTIVATION);
        }
        workflowConfiguration.getWriteDatabaseHandler().activitiWorkflow(name);
    }

    public WorkflowInstanceListener startInstance(String name, JSONObject param){
        WorkflowDefinitional definitional = workflowDefinitionalCache.get(name);
        if (definitional != null){
            WorkflowProcessing engine = definitional.getEngine();
            return engine.run(param);
        }
        return null;
    }

    public void cancelWork(String instanceId, String workflowName){
        WorkflowDefinitional definitional = workflowDefinitionalCache.get(workflowName);
        if(definitional != null){
            definitional.getEngine().cancelWork(instanceId);
        }
    }
}
