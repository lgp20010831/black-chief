package com.black.core.work.w1;

import java.util.concurrent.TimeUnit;

public class TaskTypeStaticFactory {

    public interface BlockingDefaultProcessor{
        void handler(TaskGlobalParam globalParam);
    }


    public static BlockingHandlerNode getBlockingNode(BlockingDefaultProcessor bdp){
        return getBlockingNode(bdp, null, -1);
    }

    public static BlockingHandlerNode getBlockingNode(BlockingDefaultProcessor bdp, TimeUnit timeUnit, long time){
        return getBlockingNode((BlockingHandlerConditionType) gp ->{
            bdp.handler(gp);
            return UniqueKeyUtils.bool();
        }, timeUnit, time);
    }

    public static BlockingHandlerNode getBlockingNode(BlockingHandlerConditionType bhct){
        return getBlockingNode(bhct, null, -1);
    }

    public static BlockingHandlerNode getBlockingNode(BlockingHandlerConditionType bhct, TimeUnit timeUnit, long time){
        return new BlockingHandlerNode(bhct, timeUnit, time);
    }
}
