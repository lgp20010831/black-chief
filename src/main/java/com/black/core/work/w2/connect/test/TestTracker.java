package com.black.core.work.w2.connect.test;

import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.WorkflowProcessing;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.core.work.w2.connect.tracker.WorkflowEventTracker;
import lombok.extern.log4j.Log4j2;

@Log4j2
//@TrackerIdentifying({"qj", "ycsb"})
public class TestTracker implements WorkflowEventTracker {

    @Override
    public void workflowInstanceStart(WorkflowProcessing processing, WorkflowInstanceListener listener) {
       log.info("工作流: {}, 开启实例", processing.alias());
    }

    @Override
    public void workflowInstanceFinish(WorkflowProcessing processing, WorkflowInstanceListener listener) {
        log.info("工作流: {}, 实例结束", processing.alias());
    }

    @Override
    public void taskStart(WorkflowProcessing processing, NodeInstance currentNode, WorkflowInstanceListener listener) {
        log.info("工作流: {}, 任务节点开始: {}", processing.alias(), currentNode.getRelyNode().name());
    }

    @Override
    public void taskFinish(WorkflowProcessing processing, NodeInstance currentNode, WorkflowInstanceListener listener) {
        log.info("工作流: {}, 任务节点结束: {}", processing.alias(), currentNode.getRelyNode().name());
    }
}
