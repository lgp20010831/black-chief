package com.black.core.spring.component;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.annotation.IgnorePrint;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.StopRun;
import com.black.core.spring.driver.LoadComponentDriver;
import com.black.core.tools.BeanUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;

@Log4j2
public class DefaultLoadDriver implements LoadComponentDriver {


    private static boolean cancel = false;
    private static boolean colorLog = true;

    public static boolean isCancel() {
        return cancel;
    }

    public static void closeLog(){
        cancel = true;
    }

    public static void openColorLog(){
        colorLog = true;
    }

    @Override
    public void load(Collection<Object> loadCache, ChiefExpansivelyApplication chiefExpansivelyApplication) {
        final boolean print = chiefExpansivelyApplication.obtainConfiguration().isPrintComponentLoadLog();
        for (Object c : loadCache) {
            OpenComponent openComponent = (OpenComponent) c;
            if (isStopRunComponent(openComponent)){
                continue;
            }
            String sort = getSort(openComponent);
            if (print && !cancel){
                if (log.isDebugEnabled()) {
                    log.debug("component: {}({}) load...", openComponent.getClass().getSimpleName(), sort);
                }
            }
            long startTime = System.currentTimeMillis();
            long loadbeforeMemory = Runtime.getRuntime().freeMemory() / 1024;
            try {
                openComponent.load(chiefExpansivelyApplication);
            } catch (Throwable e) {
                CentralizedExceptionHandling.handlerException(e);
                log.error(AnsiOutput.toString(AnsiColor.RED, "组件 [{}] 加载时发生错误"), openComponent.getClass().getSimpleName());
            }
            long endTime = System.currentTimeMillis();
            long loadAfterMemory = Runtime.getRuntime().freeMemory() / 1024;
            if (print && !cancel){
                Class<OpenComponent> primordialClass = BeanUtil.getPrimordialClass(openComponent);
                IgnorePrint ignorePrint = AnnotationUtils.findAnnotation(primordialClass, IgnorePrint.class);
                if (ignorePrint == null){
                    long result = loadbeforeMemory - loadAfterMemory;
                    long takeTime = endTime - startTime;
                    if (log.isInfoEnabled()) {
                        log.info(colorLog ? getColorLog() : getLog(),
                                openComponent.getClass().getSimpleName(), sort, takeTime);
                    }
                }
            }
        }
        System.gc();
        long gcAfterFreeMemory = Runtime.getRuntime().freeMemory()/1024/1024;
        log.info("free memory:{} MB", gcAfterFreeMemory);
    }

    protected boolean isStopRunComponent(Object component){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(component);
        return primordialClass.isAnnotationPresent(StopRun.class);
    }

    public String getLog(){
        return "component: {}({}) - completed take: {} KB ...";
    }

    public String getColorLog(){
        return AnsiOutput.toString(AnsiColor.GREEN, "component: ",
                AnsiColor.RED, " {} ",
                AnsiColor.BLACK, " (",
                AnsiColor.BLUE, "{}",
                AnsiColor.BLACK, ") - ",
                AnsiColor.CYAN, "completed take: ",
                AnsiColor.BLACK, "{}",
                AnsiColor.CYAN, " ms ...");
    }

    public String getSort(OpenComponent openComponent){
        LoadSort loadSort = AnnotationUtils.getAnnotation(openComponent.getClass(), LoadSort.class);
        return loadSort == null ? "-" : String.valueOf(loadSort.value());
    }
}
