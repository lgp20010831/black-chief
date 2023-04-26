package com.black.core.work.w1.test;

import com.black.core.work.w1.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BranchQueue implements ContributionWorkFlowQueue {
    @Override
    public String alias() {
        return "branch";
    }

    @Override
    public TaskFlowQueue giveTemplate(IfConditionAndHandlerAdapter adapter) {
        WorkFlowTemplateBuilder builder = adapter.begin();
        builder.addBranch(gp ->{
            List<BlockingHandlerNode> nodes = gp.toJson().getJSONArray("ids")
                    .stream()
                    .map(id -> {
                        return TaskTypeStaticFactory.getBlockingNode(tgp -> {
                            System.out.println("发送消息 --> " + id);
                        }, TimeUnit.SECONDS, 10);
                    })
                    .collect(Collectors.toList());

            nodes.add(TaskTypeStaticFactory.getBlockingNode(tgp ->{
                System.out.println("堵塞处理");
            }));
            return nodes;
        });
        return builder.end();
    }
}
