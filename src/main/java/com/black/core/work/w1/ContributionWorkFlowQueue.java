package com.black.core.work.w1;


public interface ContributionWorkFlowQueue {

    //返回一个模板的标识
    String alias();

    //提供模板
    TaskFlowQueue giveTemplate(IfConditionAndHandlerAdapter adapter);
}
