package com.black.pattern;

import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.query.AnnotationTypeWrapper;
import com.black.core.tools.BeanUtil;
import com.black.utils.LocalList;
import com.black.utils.ServiceUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("all")
public class InstanceQueue<T> {

    private final BlockingQueue<Class<? extends T>> typeQueue = new LinkedBlockingQueue<>();

    private final IoLog log = LogFactory.getArrayLog();

    private volatile boolean concurrentSecurity = false;

    private volatile BlockingQueue<T> serialInstances;

    private volatile LocalList<T> concurrentInstances;

    private Class<? extends Annotation> sortClassAnnotation = InstanceSort.class;

    public static <V> InstanceQueue<V> scan(String path){
        return scan(path, false);
    }

    public static <V> InstanceQueue<V> scan(String path, boolean concurrentSecurity){
        InstanceQueue<V> queue = new InstanceQueue<>(concurrentSecurity);
        ChiefScanner scanner = ScannerManager.getScanner();
        Set<Class<?>> types = scanner.load(path);
        for (Class<?> type : types) {
            queue.registerType((Class<? extends V>) type);
        }
        return queue;
    }

    public InstanceQueue(){
        this(false);
    }

    public InstanceQueue(boolean concurrentSecurity){
        this.concurrentSecurity = concurrentSecurity;
    }

    public void registerType(Class<? extends T> type){
        if (type != null && BeanUtil.isSolidClass(type)){
            typeQueue.add(type);
        }
    }

    public BlockingQueue<Class<? extends T>> getTypeQueue() {
        return typeQueue;
    }

    public boolean isConcurrentSecurity() {
        return concurrentSecurity;
    }

    public void setSortClassAnnotation(Class<? extends Annotation> sortClassAnnotation) {
        this.sortClassAnnotation = sortClassAnnotation;
    }

    public Collection<T> getInstances(){
        synchronized (this){
            if (isConcurrentSecurity()) {
                if (concurrentInstances == null){
                    concurrentInstances = new LocalList<>();
                    concurrentInstances.addAll(instances0());
                }
                return concurrentInstances.current();
            }else {
                if (serialInstances == null){
                    serialInstances = new LinkedBlockingQueue<>();
                    serialInstances.addAll(instances0());
                }
                return serialInstances;
            }
        }

    }

    private List<T> instances0(){
        BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
        List<T> result = new ArrayList<>();
        for (Class<? extends T> type : typeQueue) {
            try {
                T singleBean = beanFactory.getSingleBean(type);
                result.add(singleBean);
            }catch (Throwable e){
                log.error(e, "create type: {} error: {}", type, e.getMessage());
            }

        }
        return sort(result);
    }

    private List<T> sort(List<T> list){
        AnnotationTypeWrapper wrapper = AnnotationTypeWrapper.get(sortClassAnnotation);
        return ServiceUtils.sort(list, ele -> {
            Class<T> primordialClass = BeanUtil.getPrimordialClass(ele);
            Annotation annotation = primordialClass.getAnnotation(sortClassAnnotation);
            int sort = 0;
            if (annotation != null){
                Object value = wrapper.getValue("value", annotation);
                sort = Integer.parseInt(String.valueOf(value));
            }
            return sort;
        }, true);
    }

    public void reset(){
        synchronized (this){
            serialInstances = null;
            concurrentInstances = null;
        }
    }

    public void clear(){
        typeQueue.clear();
        concurrentInstances = null;
        serialInstances = null;
    }
}
