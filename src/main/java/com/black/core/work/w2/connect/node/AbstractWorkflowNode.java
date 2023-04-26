package com.black.core.work.w2.connect.node;

import com.alibaba.fastjson.JSONObject;
import com.black.core.json.Ignore;
import com.black.core.json.ToJsonObject;
import com.black.core.util.Av0;
import com.black.core.util.Body;
import com.black.core.work.w2.connect.node.instance.DefaultNodeInstance;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter  @Setter
public abstract class AbstractWorkflowNode extends Body implements WorkflowNode, ToJsonObject {

    protected String id;
    protected String workflowId;
    protected String name;
    protected int level;

    @Ignore
    protected WorkflowNodeDefinitional nodeDefinitional;
    protected boolean head, tail;

    public AbstractWorkflowNode(String name, WorkflowNodeDefinitional definitional){
        this(name, definitional, new HashMap<>());
    }

    public AbstractWorkflowNode(String name, WorkflowNodeDefinitional definitional, Map<String, Object> attributeFormData) {
        super(new HashMap<>());
        nodeDefinitional = definitional;
        for (String attributeKey : nodeDefinitional.attributeKeys()) {
            if (attributeFormData.containsKey(attributeKey)){
                put(attributeKey, attributeFormData.get(attributeKey));
            }
        }
        this.name = name;
    }

    @Override
    public JSONObject toJson() {
        return Av0.js("id", id, "workflowId", workflowId, "attr", this,
                "name", name, "level", level, "head", head, "tail", tail);
    }

    @Override
    public Collection<NodeInstance> getInstances() {
        Collection<NodeInstance> instances = new ArrayList<>();
        DefaultNodeInstance instance = new DefaultNodeInstance();
        instance.setRelyNode(this);
        instance.setHasBlocking(false);
        instances.add(instance);
        postInstances(instances);
        return instances;
    }


    protected Collection<NodeInstance> postInstances(Collection<NodeInstance> instances){
        return instances;
    }

    @Override
    public WorkflowNodeDefinitional getDefinitional() {
        return nodeDefinitional;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String workflowId() {
        return workflowId;
    }

    @Override
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int index() {
        return level;
    }

    @Override
    public void setIndex(int index) {
        this.level = index;
    }

    @Override
    public JSONObject attributes() {
        return this;
    }

    @Override
    public String getString(String key) {
        return super.getString(key);
    }

    @Override
    public int getInt(String key) {
        return getIntValue(key);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    public boolean isHead() {
        return head;
    }

    public boolean isTail() {
        return tail;
    }
}
