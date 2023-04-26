package com.black.core.work.w2.connect.cache;

import com.black.core.factory.manager.FactoryManager;
import com.black.core.json.JsonUtils;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.sql.SQLSException;
import com.black.core.tools.BeanUtil;
import com.black.core.work.utils.WorkUtils;
import com.black.core.work.w2.connect.*;
import com.black.core.work.w2.connect.check.WorkflowBalance;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.entry.*;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.utils.ScriptRunner;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.black.utils.ServiceUtils.ofMap;

@Log4j2
public class SqlWritorProxy implements WriteDatabaseHandler{

    private final WorkflowConfiguration configuration;

    private final SqlWritorType writorType;

    private WorkflowMapMapper mapsqlMapper;

    private WorkflowMapper mybatisMapper;

    private MybatisMapperBuilder mybatisBuilder;

    private MapSqlMapperBuilder mapSqlMapperBuilder;

    private final SqlServiceWriter sqlServiceWriter;

    public SqlWritorProxy(SqlServiceWriter sqlServiceWriter){
        this.sqlServiceWriter = sqlServiceWriter;
        InstanceFactory instanceFactory = FactoryManager.initAndGetInstanceFactory();
        configuration = instanceFactory.getInstance(WorkflowConfiguration.class);
        writorType = configuration.getWritorType();
        switch (writorType){
            case MYBATIS:
                mybatisBuilder = instanceFactory.getInstance(MybatisMapperBuilder.class);
                mybatisMapper = mybatisBuilder.getMapper();
                break;
            case MAP_SQL:
                mapSqlMapperBuilder = instanceFactory.getInstance(MapSqlMapperBuilder.class);
                mapsqlMapper = mapSqlMapperBuilder.getWorkflowMapMapper();
                break;
            default:
                throw new IllegalStateException("not support type: " + writorType);
        }
    }

    public boolean isMybatis(){
        return writorType == SqlWritorType.MYBATIS;
    }

    public String getWorkflowSqlName(Connection connection){
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            switch (productName){
                case "MySQL":
                    return "task-sql/workflow_mysql.sql";
                case "PostgreSQL":
                    return "task-sql/workflow_postgresql.sql";
                case "Microsoft SQL Server":
                default:
                    throw new UnsupportedOperationException("不支持当前数据库: " + productName);
            }
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    protected void checkTable(){
        Connection connection = isMybatis() ? mybatisBuilder.getTempConnection() : mapSqlMapperBuilder.getTempConnection();
        try {
            PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
            Resource resource = patternResolver.getResource(getWorkflowSqlName(connection));
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            if (!configuration.isPrint()){
                scriptRunner.setLogWriter(null);
            }
            scriptRunner.runScript(new InputStreamReader(resource.getInputStream()));
        }catch (Throwable e){
            CentralizedExceptionHandling.handlerException(e);
        }finally {
            if (isMybatis()) {
                mybatisBuilder.closeConnection(connection);
            } else {
                mapSqlMapperBuilder.closeConnection(connection);
            }
        }
    }

    public List<WorkflowEntry> queryWorkflows() {
        if (isMybatis()) {
            return getMapper().queryWorkflows();
        }else {
            return BeanUtil.toBeanBatch(getMapsqlMapper().findAll("workflow"), WorkflowEntry.class);
        }
    }

    public List<WorkflowNodeModuleEntry> queryNodeDefinitionals() {
        if (isMybatis()) {
            return getMapper().queryNodeDefinitionals();
        }else {
            return BeanUtil.toBeanBatch(getMapsqlMapper().findAll("workflow_node_module"), WorkflowNodeModuleEntry.class);
        }
    }

    public List<WorkflowNodeInstanceEntry> queryNodeInstances() {
        if (isMybatis()) {
            return getMapper().queryNodeInstances();
        }else {
            return BeanUtil.toBeanBatch(getMapsqlMapper().findAll("workflow_node_instance"), WorkflowNodeInstanceEntry.class);
        }
    }

    public List<WorkflowInstanceEntry> queryInstances() {
        if (isMybatis()) {
            return getMapper().queryInstances();
        }else {
            return BeanUtil.toBeanBatch(getMapsqlMapper().findAll("workflow_instance"), WorkflowInstanceEntry.class);
        }
    }

    public List<WorkflowRouteEntry> queryRoutes() {
        if (isMybatis()) {
            return getMapper().queryRoutes();
        }else {
            return BeanUtil.toBeanBatch(getMapsqlMapper().findAll("workflow_route"), WorkflowRouteEntry.class);
        }
    }

    @Override
    public String writeEngine(WorkflowDefinitional workflowDefinitional, boolean update) {
        String workflowId = update ? workflowDefinitional.getId() : WorkUtils.getRandomId();
        Assert.notNull(workflowId, "if update or write id is must not be null");
        workflowDefinitional.setId(workflowId);
        if (log.isInfoEnabled()) {
            log.info("workflow: {} --- id: {}", workflowDefinitional.getAlias(), workflowId);
        }

        //添加每个节点对应的依赖
        for (WorkflowNode workflowNode : workflowDefinitional.getNodeAliasSet()) {
            String nodeRelyId = addNodeRely(workflowId, workflowDefinitional.getAlias(), workflowNode, workflowDefinitional, update);
            workflowNode.setId(nodeRelyId);
        }

        //更新连线
        for (ConnectRouteWraper routeWraper : workflowDefinitional.getRouteWrapers()) {
            if (update){
                WorkflowRouteEntry workflowRouteEntry = sqlServiceWriter.getNodeRouteCache().get(routeWraper.getName());
                routeWraper.setId(workflowRouteEntry == null ? WorkUtils.getRandomId() : workflowRouteEntry.getId());
            }else {
                routeWraper.setId(WorkUtils.getRandomId());
            }

            routeWraper.setWorkflowId(workflowId);
            WorkflowRouteEntry routeEntry = WorkflowEntryConvertFactory.convertRouteEntry(routeWraper, workflowDefinitional.getNodeMap());
            if (update){
                Assert.notNull(routeEntry.getId(), "更新的时候路由 id 不能为空");
                if (isMybatis()){
                    getMapper().removeNodeRouteById(routeEntry.getId());
                }else {
                    getMapsqlMapper().deleteById("workflow_route", routeEntry.getId());
                }
            }
            if (isMybatis()){
                getMapper().writeRoute(routeEntry);
            }else {
                getMapsqlMapper().globalInsertSingle("workflow_route", JsonUtils.toJson(routeEntry));
            }
            sqlServiceWriter.getNodeRouteCache().put(routeEntry.getName(), routeEntry);
        }

        if(update){
            if (isMybatis()) {
                getMapper().removeWorkflow(workflowId);
            }else {
                getMapsqlMapper().deleteById("workflow", workflowId);
            }
        }
        WorkflowEntry entry = new WorkflowEntry(workflowId, workflowDefinitional.getAlias(),
                workflowDefinitional.getStatus(), null, null);
        if (isMybatis()){
            getMapper().writeEntry(entry);
        }else {
            getMapsqlMapper().globalInsertSingle("workflow", JsonUtils.toJson(entry));
        }
        return workflowId;
    }

    public String addNodeRely(String workflowId,
                              String alias,
                              WorkflowNode workflowNode,
                              WorkflowDefinitional workflowDefinitional,
                              boolean update){
        WorkflowNodeInstanceEntry saveEntry = sqlServiceWriter.getNodeInstanceEntryCache().get(workflowNode.name());
        WorkflowNodeInstanceEntry writeEntry = WorkflowEntryConvertFactory.paserNode(workflowNode, workflowDefinitional);
        if (saveEntry != null && update){
            writeEntry.setId(saveEntry.getId());
            if (isMybatis()) {
                getMapper().removeNodeInstanceById(writeEntry.getId());
            }else {
                getMapsqlMapper().deleteById("workflow_node_instance", writeEntry.getId());
            }
        }
        if (isMybatis()){
            getMapper().writeNodeInstance(writeEntry);
        }else {
            getMapsqlMapper().globalInsertSingle("workflow_node_instance", JsonUtils.toJson(writeEntry));
        }
        sqlServiceWriter.getNodeInstanceEntryCache().put(alias, writeEntry);
        return writeEntry.getId();
    }


    @Override
    public String writeNode(WorkflowNode workflowNode, WorkflowDefinitional definitional) {
        WorkflowNodeInstanceEntry entry = WorkflowEntryConvertFactory.paserNode(workflowNode, definitional);
        if (isMybatis()){
            getMapper().writeNodeInstance(entry);
        }else {
            getMapsqlMapper().globalInsertSingle("workflow_node_instance", JsonUtils.toJson(entry));
        }
        return entry.getId();
    }

    @Override
    public String writeRoute(ConnectRouteWraper routeWraper, WorkflowDefinitional definitional) {
        WorkflowRouteEntry routeEntry = WorkflowEntryConvertFactory.convertRouteEntry(routeWraper.rom(), definitional.getNodeMap());
        if (isMybatis()){
            getMapper().writeRoute(routeEntry);
        }else {
            getMapsqlMapper().globalInsertSingle("workflow_route", JsonUtils.toJson(routeEntry));
        }
        return routeEntry.getId();
    }

    @Override
    public String writeInstance(WorkflowInstanceListener instanceListener) {
        WorkflowInstance instance = instanceListener.getInstance();
        String id = instance.id();
        WorkflowInstanceEntry instanceEntry = WorkflowEntryConvertFactory.convertInstanceEntry(instanceListener);
        Map<String, WorkflowInstanceEntry> activityInstanceCache = sqlServiceWriter.getActivityInstanceCache();
        if (isMybatis()) {
            getMapper().writeInstance(instanceEntry);
        }else {
            getMapsqlMapper().globalInsertSingle("workflow_instance", JsonUtils.toJson(instanceEntry));
        }
        activityInstanceCache.put(id, instanceEntry);
        return id;
    }

    @Override
    public String writeNodeModule(WorkflowNodeDefinitional definitional) {
        WorkflowNodeModuleEntry moduleEntry = WorkUtils.parse(definitional, WorkflowNodeModuleEntry.class);
        if (moduleEntry.getId() == null) {
            moduleEntry.setId(WorkUtils.getRandomId());
        }
        if (isMybatis()){
            getMapper().writeNodeModule(moduleEntry);
        }else {
            getMapsqlMapper().globalInsertSingle("workflow_node_module", JsonUtils.toJson(moduleEntry));
        }
        sqlServiceWriter.getNodeModuleEntryCache().put(moduleEntry.getName(), moduleEntry);
        return moduleEntry.getId();
    }

    @Override
    public WorkflowInstance getInstance(String workflowId, String instanceId) {
        Map<String, WorkflowInstanceEntry> activityInstanceCache = sqlServiceWriter.getActivityInstanceCache();
        if (!activityInstanceCache.containsKey(instanceId)){
            WorkflowInstanceEntry instanceEntry;
            if (isMybatis()){
                instanceEntry = getMapper().queryInstanceById(instanceId);
            }else {
                instanceEntry = BeanUtil.toBean(getMapsqlMapper().findById("workflow_instance", instanceId), WorkflowInstanceEntry.class);
            }
            activityInstanceCache.put(instanceId, instanceEntry);
        }
        return WorkflowEntryConvertFactory.convertInstance(activityInstanceCache.get(instanceId));
    }

    @Override
    public WorkflowInstanceListener getListener(String instanceId, WorkflowProcessing workflowProcessing) {
        if (isMybatis()){
            WorkflowInstanceEntry entry = mybatisMapper.queryInstanceById(instanceId);
            if (entry == null){
                return null;
            }
            sqlServiceWriter.getActivityInstanceCache().put(entry.getId(), entry);
            return WorkflowEntryConvertFactory.convertInstanceListener(entry);
        }else {
            Map<String, Object> map = mapsqlMapper.findById("workflow_instance", instanceId);
            if (map == null) return null;
            WorkflowInstanceEntry entry = BeanUtil.mapping(new WorkflowInstanceEntry(), map);
            sqlServiceWriter.getActivityInstanceCache().put(entry.getId(), entry);
            return WorkflowEntryConvertFactory.convertInstanceListener(entry);
        }
    }

    @Override
    public List<WorkflowNodeInstanceDetailsEntry> readDetailsEntry(String instanceId) {
        if (isMybatis()) {
            return getMapper().queryNodeInstanceDetailsByInstanceId(instanceId);
        }else {
            Object list = getMapsqlMapper().globalSelect("workflow_node_instance_details", ofMap("instanceId", instanceId));
            return BeanUtil.toBeanBatch((Collection<Object>) list, WorkflowNodeInstanceDetailsEntry.class);
        }
    }

    @Override
    public void updateInstance(WorkflowInstanceListener listener) {
        WorkflowInstanceEntry entry = WorkflowEntryConvertFactory.convertInstanceEntry(listener);
        if (isMybatis()) {
            getMapper().updateInstance(entry);
        }else {
            getMapsqlMapper().globalUpdate("workflow_instance", JsonUtils.toJson(entry, true), ofMap("id", entry.getWorkflowId()));
        }
        sqlServiceWriter.getActivityInstanceCache().put(entry.getId(), entry);
    }

    @Override
    public void updateNodeModule(WorkflowNodeDefinitional definitional) {
        WorkflowNodeModuleEntry moduleEntry = WorkUtils.parse(definitional, WorkflowNodeModuleEntry.class);
        if (moduleEntry.getId() == null) {
            moduleEntry.setId(WorkUtils.getRandomId());
        }
        if (isMybatis()) {
            getMapper().updateNodeModule(moduleEntry);
        }else {
            getMapsqlMapper().globalUpdate("workflow_node_module", JsonUtils.toJson(moduleEntry, true),
                    ofMap("id", moduleEntry.getId()));
        }
        sqlServiceWriter.getNodeModuleEntryCache().put(moduleEntry.getName(), moduleEntry);
    }

    @Override
    public void removeNode(String alias) {
        if (isMybatis()){
            getMapper().removeNode(alias);
        }else{
            getMapsqlMapper().globalDelete("workflow_node", ofMap("name", alias));
        }
    }

    @Override
    public WorkflowConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public WorkflowBalance getBalance() {
        return null;
    }

    @Override
    public WorkflowMapper getMapper() {
        return mybatisMapper;
    }

    public WorkflowMapMapper getMapsqlMapper() {
        return mapsqlMapper;
    }

    @Override
    public void startWork(WorkflowInstanceListener listener) {
        //缓存实例对象
        writeInstance(listener);

        //将节点实例信息写入数据库
        for (NodeInstance nodeInstance : listener.getNodeInstances().values()) {
            WorkflowNodeInstanceDetailsEntry entry = jg(listener, nodeInstance);
            if (isMybatis()) {
                getMapper().writeNodeInstanceDetails(entry);
            }else {
                getMapsqlMapper().globalInsertSingle("workflow_node_instance_details", JsonUtils.toJson(entry));
            }
        }
    }

    protected WorkflowNodeInstanceDetailsEntry jg(WorkflowInstanceListener listener, NodeInstance instance){
        WorkflowNodeInstanceDetailsEntry entry = WorkUtils.parse(instance, WorkflowNodeInstanceDetailsEntry.class);
        entry.setNodeName(instance.getRelyNode().name());
        entry.setInstanceId(listener.getInstance().id());
        entry.setNodeInstanceId(instance.getRelyNode().id());
        entry.setLevel(instance.getRelyNode().index());
        return entry;
    }

    @Override
    public void finishTask(WorkflowInstanceListener listener, String result) {
        String id = listener.getInstance().id();

        //删除工作流实例对象
        if (isMybatis()){
            getMapper().removeInstance(id);
        }else {
            getMapsqlMapper().deleteById("workflow_instance", id);
        }

        //删除节点实例对象
        if (isMybatis()){
            getMapper().removeNodeInstanceDetails(id);
        }else {
            getMapsqlMapper().globalDelete("workflow_node_instance_details", ofMap("instanceId", id));
        }



        //清除缓存
        sqlServiceWriter.getActivityInstanceCache().remove(id);

        try {
            WorkflowInstanceEntry instanceEntry = WorkflowEntryConvertFactory.convertInstanceEntry(listener);
            WorkflowHistoryInstanceEntry historyInstanceEntry =
                    WorkUtils.parse(instanceEntry, WorkflowHistoryInstanceEntry.class);
            historyInstanceEntry.setInstanceId(instanceEntry.getId());
            historyInstanceEntry.setResult(result == null ? (listener.finallyResult() ? "成功" : "失败") : result);
            if (isMybatis()){
                getMapper().writeHistoryInstance(historyInstanceEntry);
            }else {
                getMapsqlMapper().globalInsertSingle("workflow_history_instance", JsonUtils.toJson(historyInstanceEntry));
            }
        }catch (RuntimeException ex){
            log.info("write history fail");
            CentralizedExceptionHandling.handlerException(ex);
        }
    }

    @Override
    public void pauseTask(WorkflowInstanceListener listener) {
//首先更新了所有节点实例的信息
        for (NodeInstance instance : listener.getNodeInstances().values()) {
            WorkflowNodeInstanceDetailsEntry entry = jg(listener, instance);
            if (isMybatis()) {
                getMapper().updateNodeInstanceDetails(entry);
            }else {
                getMapsqlMapper().globalUpdate("workflow_node_instance_details", JsonUtils.toJson(entry),
                        ofMap("instanceId", entry.getInstanceId(), "nodeInstanceId", entry.getNodeInstanceId()));
            }
        }

        //在更新实例的信息
        updateInstance(listener);
    }

    @Override
    public void init(WorkflowProcessing engine) {

    }

    @Override
    public void parkWorkflow(String name) {
        if (isMybatis()){
            getMapper().updateWorkflowStatus(WorkflowStatus.HANG, name);
        }else {
            getMapsqlMapper().globalUpdate("workflow", ofMap("status", WorkflowStatus.HANG), ofMap("name", name));
        }
        WorkflowEntry workflowEntry = sqlServiceWriter.getWorkflowEntryCache().get(name);
        if (workflowEntry != null){
            workflowEntry.setStatus(WorkflowStatus.HANG);
        }
    }

    @Override
    public void activitiWorkflow(String name) {
        if (isMybatis()){
            getMapper().updateWorkflowStatus(WorkflowStatus.ACTIVATION, name);
        }else {
            getMapsqlMapper().globalUpdate("workflow", ofMap("status", WorkflowStatus.ACTIVATION), ofMap("name", name));
        }
        WorkflowEntry workflowEntry = sqlServiceWriter.getWorkflowEntryCache().get(name);
        if (workflowEntry != null){
            workflowEntry.setStatus(WorkflowStatus.ACTIVATION);
        }
    }
}
