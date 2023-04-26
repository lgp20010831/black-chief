package com.black.callback;

import com.black.function.Consumer;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.core.cache.ClassSourceCache;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.util.CentralizedExceptionHandling;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class ProjectScanFeedbackRegister implements SpringApplicationRunListener {

    private static final IoLog log = LogFactory.getArrayLog();

    private final static LinkedBlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

    public static void registerTask(@NonNull String range, @NonNull Consumer<Set<Class<?>>> callback){
        taskQueue.add(new Task(range, callback));
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        run();
    }

    public static void run(){
        if (GlobalCallBackLogManager.printLog)
        log.info("[ProjectScanFeedbackRegister] run scan project feedback tasks:{}", taskQueue.size());
        ChiefScanner scanner = ScannerManager.getScanner();
        Task task;
        while ((task = taskQueue.poll()) != null){
            String range = task.getRange();
            Consumer<Set<Class<?>>> callback = task.getCallback();
            try {
                Set<Class<?>> source = ClassSourceCache.getSource(range);
                if (source == null){
                    source = scanner.load(range);
                }
                if (GlobalCallBackLogManager.printLog)
                log.debug("[ProjectScanFeedbackRegister] run task -- executor: {}", callback);
                callback.accept(source);
            }catch (Throwable ex){
                CentralizedExceptionHandling.handlerException(ex);
                log.trace("[ProjectScanFeedbackRegister] run task: {} fair", callback);
            }
        }

    }


    @AllArgsConstructor @Getter
    static class Task{
        private final String range;
        private final Consumer<Set<Class<?>>> callback;
    }

}
