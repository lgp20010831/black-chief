package com.black.core.work.w1;

import com.black.core.work.w2.connect.*;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.node.DefaultHandlerWorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

//处理默认节点的添加逻辑
public class DefaultWorkflowBuilder implements WorkflowBuilder {

    protected String alias;

    protected WorkflowDefinitional workflowDefinitional;

    protected WorkflowRefinedDispatcher refinedDispatcher;

    protected Map<String, WorkflowNodeDefinitional> nodeDefinitionals;

    protected WorkflowDrawer drawer;

    protected final WorkflowConfiguration configuration;

    protected final ThreadLocal<PreNodeWrapper> preNodeLocal = new ThreadLocal<>();

    public DefaultWorkflowBuilder(String status, String alias, WorkflowConfiguration configuration){
        this.alias = alias;
        this.configuration = configuration;
        workflowDefinitional = new WorkflowDefinitional(status, alias);
        refinedDispatcher = WorkflowRefinedManager.getDispatcher();
        nodeDefinitionals = refinedDispatcher.getNodeDefinitionals();
        drawer = new DefaultWorkflowDrawer(nodeDefinitionals, this, alias);
    }

    @AllArgsConstructor @Getter
    protected static class PreNodeWrapper{
        WorkflowNode preNode;
        Condition condition;
    }

    @Override
    public WorkflowDefinitional build() {
        preNodeLocal.remove();
        workflowDefinitional.end();
        return workflowDefinitional;
    }

    @Override
    public WorkflowDefinitional getDefinitional() {
        return workflowDefinitional;
    }

    @Override
    public WorkflowDrawer getDrawer() {
        return drawer;
    }

    public WorkflowNode relevance(WorkflowNode nextNode, Condition condition){
        workflowDefinitional.addNode(nextNode);
        if (nextNode instanceof DefaultHandlerWorkflowNode){
            doHandler(nextNode, condition);
        }
        return nextNode;
    }

    public void doHandler(WorkflowNode nextNode, Condition condition){
        PreNodeWrapper preNodeWrapper = preNodeLocal.get();
        if (preNodeWrapper != null){
            ConnectRouteWraper routeWraper = ConnectStaticFactory.create(preNodeWrapper.preNode.name(), nextNode.name(), alias, condition);
            workflowDefinitional.addConnect(routeWraper);
            preNodeLocal.remove();
        }
        preNodeLocal.set(new PreNodeWrapper(nextNode, condition));
    }

    @Override
    public WorkflowNode put(String moduleName, String nodeName, Map<String, Object> attributeFormData, Condition condition) {
        return drawer.put(moduleName, nodeName, attributeFormData, condition);
    }

    @Override
    public WorkflowNode put(String moduleName, String nodeName, Condition condition) {
        return drawer.put(moduleName, nodeName, condition);
    }

    @Override
    public WorkflowNode put(String moduleName, Map<String, Object> attributeFormData, Condition condition) {
        return drawer.put(moduleName, attributeFormData, condition);
    }

    @Override
    public WorkflowNode put(String moduleName, Condition condition) {
        return drawer.put(moduleName, condition);
    }

    @Override
    public WorkflowNode put(String moduleName, Map<String, Object> attributeFormData) {
        return put(moduleName, attributeFormData, (ler, ni) -> ni.getResult());
    }

    @Override
    public WorkflowNode put(String moduleName) {
        return put(moduleName, (ler, ni) -> ni.getResult());
    }
}
