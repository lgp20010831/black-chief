package com.black.core.work.w2.connect.tracker;

import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.WorkflowProcessing;
import com.black.core.work.w2.connect.node.instance.NodeInstance;

public interface WorkflowEventTracker {

    /** 当一个工作流开启一个实例, 触发回调 */
    default void workflowInstanceStart(WorkflowProcessing processing, WorkflowInstanceListener listener){}

    /** 当一个节点的任务开始时, 触发该回调 **/
    default void taskStart(WorkflowProcessing processing, NodeInstance currentNode, WorkflowInstanceListener listener){}

    /** 当一个节点任务结束时 */
    default void taskFinish(WorkflowProcessing processing, NodeInstance currentNode, WorkflowInstanceListener listener){}

    /** 当一个工作流实例结束时触发回调 **/
    default void workflowInstanceFinish(WorkflowProcessing processing, WorkflowInstanceListener listener){}
}
