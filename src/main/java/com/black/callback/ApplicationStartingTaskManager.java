package com.black.callback;

import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.spring.ChiefSpringApplication;
import com.black.core.annotation.Sort;
import com.black.core.util.CentralizedExceptionHandling;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Log4j2 @Sort(100)
public class ApplicationStartingTaskManager implements SpringApplicationRunListener {
    public static final LinkedBlockingQueue<Consumer<Class<?>>> taskQueue = new LinkedBlockingQueue<>();

    public static void addTask(Consumer<Class<?>> task){
        taskQueue.add(task);
    }

    public static boolean isChiefEnv(){
        return SpringApplicationHodler.getSpringApplication() instanceof ChiefSpringApplication;
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        String[] ranges = SpringApplicationHodler.getSpringProjectRange();
        if (GlobalCallBackLogManager.printLog){
            log.info("application starting scan ranges: {}", Arrays.toString(ranges));
        }
        doScan(ranges);
    }

    private static void doScan(String[] ranges){
        ChiefScanner scanner = ScannerManager.getScanner();
        for (String range : ranges) {
            Set<Class<?>> classes = scanner.load(range);
            doLoadClasses(classes);
        }
    }

    private static void doLoadClasses(Set<Class<?>> classes){
        for (Class<?> clazz : classes) {
            for (Consumer<Class<?>> classConsumer : taskQueue) {
                try {
                    classConsumer.accept(clazz);
                }catch (Throwable e){
                    CentralizedExceptionHandling.handlerException(e);
                    log.info("processor clazz fair: {}", e.getMessage());
                }
            }
        }
    }
}
