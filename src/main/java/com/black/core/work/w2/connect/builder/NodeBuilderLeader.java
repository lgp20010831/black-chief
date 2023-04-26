package com.black.core.work.w2.connect.builder;

import com.black.core.work.w2.connect.Condition;
import com.black.core.work.w2.connect.ConnectRouteWraper;
import com.black.core.work.w2.connect.ConnectStaticFactory;
import com.black.core.work.w2.connect.WorkflowDefinitional;
import com.black.core.work.w2.connect.ill.UnknowNodeDefinitionalException;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.node.WorkflowNodeDefinitional;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class NodeBuilderLeader {

    private final String alias;
    private final String status;
    private final Map<String, WorkflowNodeDefinitional> definitionalMap;
    private final NodeBuilderManager builderManager;
    private final Collection<NodeBuilder> builders;
    private final ThreadLocal<PreWrapper> wrapperLocal = new ThreadLocal<>();
    private final ThreadLocal<Integer> levelLocal = new ThreadLocal<>();
    private final WorkflowDefinitional workflowDefinitional;

    public NodeBuilderLeader(String alias, String status,
                             Map<String, WorkflowNodeDefinitional> definitionalMap,
                             NodeBuilderManager builderManager,
                             Collection<NodeBuilder> builders) {
        this.alias = alias;
        this.status = status;
        this.definitionalMap = definitionalMap;
        this.builderManager = builderManager;
        this.builders = builders;
        workflowDefinitional = new WorkflowDefinitional(status, alias);
        levelLocal.set(0);
    }

    public WorkflowNode add(String name,  Map<String, Object> attributes){
        return add(name, null, attributes);
    }

    public WorkflowNode add(String name, Map<String, Object> attributes, Condition condition){
        return add(name, null, attributes, condition);
    }

    public WorkflowNode add(String name, String nodeName, Map<String, Object> attributes){
        return add(name, null, attributes, (ler, ni) -> ni.getResult());
    }

    public WorkflowNode add(String name, String nodeName, Map<String, Object> attributes, Condition condition){
        WorkflowNodeDefinitional nodeDefinitional = definitionalMap.get(name);
        if (nodeDefinitional == null){
            throw new UnknowNodeDefinitionalException("找不到节点模板: " + name);
        }

        checkAttributes(nodeDefinitional, attributes);
        WorkflowNode node = null;
        for (NodeBuilder builder : builders) {
            if (builder.support(nodeDefinitional, this)) {
                node = builder.doBuild(nodeDefinitional, this, attributes, condition, nodeName);
                break;
            }
        }
        return node;
    }

    protected void checkAttributes(WorkflowNodeDefinitional definitional, Map<String, Object> attributes){
        List<String> attributeKeys = definitional.attributeKeys();
        for (String key : attributes.keySet()) {
            if (!attributeKeys.contains(key)){
                throw new RuntimeException("构造工作流,使用节点时,初始属性并没有在节点中定义, 未定义的属性: " + key);
            }
        }
    }

    public void createConnect(WorkflowNode start, WorkflowNode end, @NonNull Condition condition){
        ConnectRouteWraper routeWraper = ConnectStaticFactory.create(start.name(), end.name(), alias, condition);
        workflowDefinitional.addConnect(routeWraper);
    }

    public void removeConnect(WorkflowNode start, WorkflowNode end){
        workflowDefinitional.removeConnect(start.name(), end.name());
    }

    public WorkflowDefinitional end(){
        workflowDefinitional.end();
        return workflowDefinitional;
    }

}
