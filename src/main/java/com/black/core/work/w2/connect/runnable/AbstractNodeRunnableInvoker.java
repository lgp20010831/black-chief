package com.black.core.work.w2.connect.runnable;

import com.black.core.work.utils.WorkUtils;
import com.black.core.work.w2.connect.GlobalWorkflowManagementCenter;
import com.black.core.work.w2.connect.WorkflowInstanceListener;
import com.black.core.work.w2.connect.WorkflowProcessing;
import com.black.core.work.w2.connect.WorkflowRunnable;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.ill.BlockingException;
import com.black.core.work.w2.connect.ill.WorkflowNodeRunnableException;
import com.black.core.work.w2.connect.node.AbstractWorkflowNode;
import com.black.core.work.w2.connect.node.instance.DefaultNodeInstance;
import com.black.core.work.w2.connect.node.instance.NodeInstance;
import com.black.core.work.w2.connect.node.WorkflowNode;
import com.black.core.work.w2.connect.tracker.WorkflowEventTracker;

public abstract class AbstractNodeRunnableInvoker implements WorkflowNodeRunnableHandler{

    @Override
    public void invokeRunnable(WorkflowInstanceListener listener, NodeInstance nodeInstance)
            throws WorkflowNodeRunnableException, BlockingException {
        lock.lock();
        try {
            doInvokeRunnable(listener, nodeInstance);
        }finally {
            lock.unlock();
        }
    }

    protected void doInvokeRunnable(WorkflowInstanceListener listener, NodeInstance nodeInstance)
            throws WorkflowNodeRunnableException, BlockingException {

        DefaultNodeInstance dni = (DefaultNodeInstance) nodeInstance;
        if (!dni.isInvoke()){
            WorkflowNode workflowNode = nodeInstance.getRelyNode();
            try {
                //通知监听者任务开始了
                notifyTracker(listener, nodeInstance);
                WorkflowRunnable runnable = workflowNode.getDefinitional().getRunnable();
                if(runnable != null){
                    dni.setInvokeTime(WorkUtils.getTime());
                    runnable.run(listener, (AbstractWorkflowNode) workflowNode);
                }
            }catch (Throwable e){
                throw new WorkflowNodeRunnableException(e);
            }finally {
                dni.setInvoke(true);
                dni.setFinishTime(WorkUtils.getTime());
            }
        }
    }

    protected void notifyTracker(WorkflowInstanceListener listener, NodeInstance instance){
        WorkflowProcessing processing = listener.getWorkflowProcessing();
        WorkflowConfiguration configuration = processing.getConfiguration();
        if (configuration.isAsynTracker()) {
            GlobalWorkflowManagementCenter.getInstance().asynInvokeTracker(() ->{
                for (WorkflowEventTracker tracker : configuration.getTrackerList(processing)) {
                    tracker.taskStart(processing, instance, listener);
                }
            });
        }else {
            for (WorkflowEventTracker tracker : configuration.getTrackerList(processing)) {
                tracker.taskStart(processing, instance, listener);
            }
        }
    }
}
