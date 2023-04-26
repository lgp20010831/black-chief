package com.black.core.work.w2.connect.runnable;

import com.black.core.work.utils.WorkUtils;
import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.WorkflowRunnable;
import com.black.core.work.w2.connect.ill.WorkflowAttributeKeys;
import com.black.core.work.w2.connect.node.AbstractWorkflowNode;
import com.black.core.work.w2.connect.node.instance.DefaultNodeInstance;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.annotation.WorkflowRunnableInvoker;
import com.black.core.work.w2.connect.ill.BlockingException;
import com.black.core.work.w2.connect.ill.WorkflowNodeRunnableException;
import com.black.core.work.w2.connect.node.TimeOutHandlerWorkflowNode;

import java.util.concurrent.TimeUnit;

@WorkflowRunnableInvoker
public class TimOutNodeRunnableInvoker extends AbstractNodeRunnableInvoker{

    @Override
    public boolean support(WorkflowInstanceListener listener, NodeInstance workflowNode) {
        return workflowNode.getRelyNode() instanceof TimeOutHandlerWorkflowNode;
    }

    @Override
    public void doInvokeRunnable(WorkflowInstanceListener listener, NodeInstance nodeInstance)
            throws WorkflowNodeRunnableException, BlockingException {

        WorkflowNode relyNode = nodeInstance.getRelyNode();
        DefaultNodeInstance dni = (DefaultNodeInstance) nodeInstance;
        if (!nodeInstance.isInvoke()) {

            try {
                //如果该节点并没有执行逻辑
                try {
                    notifyTracker(listener, nodeInstance);
                    WorkflowRunnable runnable = relyNode.getDefinitional().getRunnable();
                    if(runnable != null){
                        dni.setInvokeTime(WorkUtils.getTime());
                        runnable.run(listener, (AbstractWorkflowNode) relyNode);
                    }
                }catch (Throwable e){
                    throw new WorkflowNodeRunnableException(e);
                }
                TimeUnit unit = relyNode.attributes().getObject(WorkflowAttributeKeys.TIME_OUT_UNIT, TimeUnit.class);
                if (unit != null){
                    Long deadLine = relyNode.attributes().getLong(WorkflowAttributeKeys.TIME_OUT_TIME);
                    listener.getWorkflowProcessing().submitTimerTask(listener, relyNode.id(), unit, deadLine);
                }
                throw new BlockingException();
            }finally {
                dni.setInvoke(true);
                dni.setHasBlocking(true);
                dni.setFinishTime(WorkUtils.getTime());
            }
        }else {

            //将其堵塞标识恢复
            dni.setHasBlocking(false);
        }
    }
}
