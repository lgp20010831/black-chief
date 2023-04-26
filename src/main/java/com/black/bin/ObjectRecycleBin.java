package com.black.bin;

import com.black.function.Releasable;
import com.black.core.tools.BeanUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

//回收站这个东西确实不适合存在, 长期保留对象会占用大量内存
public class ObjectRecycleBin {

    private static final Map<Class<?>, LinkedBlockingQueue<Object>> bin = new ConcurrentHashMap<>();

    public static <T> T apply(Class<T> type){
        LinkedBlockingQueue<T> queue = getQueue(type);
        T poll = queue.poll();
        if (poll == null){
            poll = instanceBean(type);
        }
        return openBean(poll);
    }

    private static <T> T openBean(T bean){
        if (bean instanceof Releasable){
            ((Releasable) bean).open();
            if (!ApplyProxyFactory.isProxy(bean)){
                return ApplyProxyFactory.proxy(bean, new ReleasableBeanLayer());
            }
        }
        return bean;
    }

    private static <T> T instanceBean(Class<T> type){
        Configuration configuration = RecycleBinConfigurationHolder.getConfiguration();
        return InstanceBeanManager.instance(type, configuration.getInstanceType());
    }

    public static void release(Object bean){
        if (bean == null){
            return;
        }

        Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
        LinkedBlockingQueue<Object> queue = getQueue(primordialClass);
        if (isDiscardOfBean(queue)){
            discardBean(bean);
        }else {
            queue.add(bean);
        }
    }

    private static void discardBean(Object bean){
        if (bean instanceof Releasable){
            ((Releasable) bean).discard();
        }
    }

    private static boolean isDiscardOfBean(LinkedBlockingQueue<Object> queue){
        Configuration configuration = RecycleBinConfigurationHolder.getConfiguration();
        int coreObjectSize = configuration.getCoreObjectSize();
        return queue.size() >= coreObjectSize;
    }

    public static <T> LinkedBlockingQueue<T> getQueue(Class<T> type){
        Configuration configuration = RecycleBinConfigurationHolder.getConfiguration();
        return (LinkedBlockingQueue<T>) bin.computeIfAbsent(type, t -> {
            LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
            int coreObjectSize = configuration.getCoreObjectSize();
            for (int i = 0; i < coreObjectSize; i++) {
                queue.add(instanceBean(t));
            }
            return queue;
        });
    }

}
