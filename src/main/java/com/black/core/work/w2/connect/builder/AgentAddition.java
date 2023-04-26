package com.black.core.work.w2.connect.builder;

import com.black.core.work.w2.connect.Condition;
import com.black.core.work.w2.connect.node.WorkflowNode;

/***
 * 这个接口的作用是能让上一个节点通过函数来控制下一个节点
 * 是如何添加到队列中的
 */
public interface AgentAddition {

    void addition(WorkflowNode nextNode, Condition condition);

}
