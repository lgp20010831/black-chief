package com.black.core.work.w1.test;

import com.alibaba.fastjson.JSONArray;
import com.black.core.work.w1.*;

import java.util.concurrent.TimeUnit;

public class CreateQueue implements ContributionWorkFlowQueue {

    @Override
    public String alias() {
        return "ycsb";
    }

    @Override
    public TaskFlowQueue giveTemplate(IfConditionAndHandlerAdapter adapter) {
        WorkFlowTemplateBuilder builder = adapter.begin();

        //现在实现异常上报流程, 5秒内如果没有处理则走下一个流程
        //第一个节点
        builder.obtainHandler(tgp ->{
            JSONArray array = tgp.toJson().getJSONArray("userid");
            Object userid = array.get(0);
            System.out.println("发送异常上报:" + userid);
            return UniqueKeyUtils.bool();
        }, 40, TimeUnit.SECONDS);

        //如果 上报 false
        //第二个节点
        IfConditionNode ifNode = builder.obtainIf(tgp -> tgp.toJson().containsKey("bossid"));

        builder.addDynamic();

        //第三个节点
        builder.setFalse(ifNode, (BlockingHandlerConditionType) tgp ->{
            System.out.println("false 处理....");
            return UniqueKeyUtils.bool();
        }, 10, TimeUnit.SECONDS);

        builder.setTrue(ifNode, (BlockingHandlerConditionType)tgp ->{
            String array = tgp.toJson().getString("bossid");
            System.out.println("发送异常上报给 boss:" + array);
            return UniqueKeyUtils.bool();
        }, 10, TimeUnit.SECONDS);


        //模板建好了
        return builder.end();
    }
}
