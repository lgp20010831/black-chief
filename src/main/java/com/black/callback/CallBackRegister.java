package com.black.callback;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.util.CentralizedExceptionHandling;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.LinkedBlockingQueue;


public class CallBackRegister implements SpringApplicationRunListener {

    private static final LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    private static final IoLog log = LogFactory.getArrayLog();

    public static LinkedBlockingQueue<Runnable> getTasks() {
        return tasks;
    }

    public static void addTask(Runnable runnable){
        tasks.add(runnable);
    }

    public CallBackRegister(){}


    @Override
    public void running(ConfigurableApplicationContext context) {
        start();
    }

    public static void start(){
        if (GlobalCallBackLogManager.printLog)
        log.info("[CallBackRegister] Callback function registry started execution Number of tasks: {}", tasks.size());
        for (Runnable task : tasks) {
            try {
                if (GlobalCallBackLogManager.printLog)
                log.debug("[CallBackRegister] Currently executing task: {}", task);
                task.run();
            }catch (Throwable e){
                CentralizedExceptionHandling.handlerException(e);
                log.trace("[CallBackRegister] An error occurred while executing the task -- {}", task);
            }
        }

        if (GlobalCallBackLogManager.printLog)
        log.info("[CallBackRegister] All tasks completed");
        tasks.clear();
    }
}
