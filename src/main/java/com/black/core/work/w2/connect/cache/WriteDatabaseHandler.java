package com.black.core.work.w2.connect.cache;

import com.black.core.work.w2.connect.WorkflowProcessing;
import com.black.core.work.w2.connect.check.WorkflowBalance;
import com.black.core.work.w2.connect.config.WorkflowConfiguration;
import com.black.core.work.w2.connect.WorkflowInstanceListener;

public interface WriteDatabaseHandler extends InvokeWorkflowMapper {

    WorkflowConfiguration getConfiguration();

    WorkflowBalance getBalance();

    WorkflowMapper getMapper();

    void startWork(WorkflowInstanceListener listener);

    //写入
    void finishTask(WorkflowInstanceListener listener, String result);

    void pauseTask(WorkflowInstanceListener listener);

    void init(WorkflowProcessing engine);

    void parkWorkflow(String name);

    void activitiWorkflow(String name);
}
