package com.black.core.work.w2.connect.node;

import com.alibaba.fastjson.JSONObject;
import com.black.core.work.w2.connect.node.instance.NodeInstance;

import java.util.Collection;

public interface WorkflowNode {

    //节点 id
    String id();

    void setId(String id);

    String workflowId();

    boolean isHead();

    boolean isTail();

    void setHead(boolean head);

    void setTail(boolean tail);

    void setWorkflowId(String workflowId);

    String name();

    int index();

    void setIndex(int index);

    WorkflowNodeDefinitional getDefinitional();

    //是否堵塞
    boolean isBlocking();

    JSONObject attributes();

    String  getString(String key);

    int getInt(String key);

    Collection<NodeInstance> getInstances();
}
