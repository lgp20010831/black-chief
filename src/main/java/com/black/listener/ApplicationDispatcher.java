package com.black.listener;

import com.black.core.tools.BeanUtil;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.listener.inter.Listener;
import com.black.listener.inter.MarkMatchingListener;
import com.black.listener.inter.TypeMatchingListener;
import com.black.utils.ReflexHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ApplicationDispatcher {

    private static final Map<Class<?>, List<TypeMatchingListener<Object>>> typeMatchingListenerCache = new ConcurrentHashMap<>();

    private static final LinkedBlockingQueue<MarkMatchingListener<Object>> markMatchingListenerCache = new LinkedBlockingQueue<>();

    public static void registerListener(Listener<?> listener){
        if (listener != null){
            if (listener instanceof TypeMatchingListener) {
                Class<?>[] genericVals = ReflexHandler.genericVal(listener.getClass(), TypeMatchingListener.class);
                if (genericVals.length != 1){
                    throw new IllegalStateException("generic is not 1");
                }
                List<TypeMatchingListener<Object>> typeMatchingListeners = typeMatchingListenerCache.computeIfAbsent(genericVals[0], gv -> new ArrayList<>());
                typeMatchingListeners.add((TypeMatchingListener<Object>) listener);
            }

            if (listener instanceof MarkMatchingListener){
                markMatchingListenerCache.add((MarkMatchingListener<Object>) listener);
            }
        }
    }

    public static LinkedBlockingQueue<MarkMatchingListener<Object>> getMarkMatchingListenerCache() {
        return markMatchingListenerCache;
    }

    public static Map<Class<?>, List<TypeMatchingListener<Object>>> getTypeMatchingListenerCache() {
        return typeMatchingListenerCache;
    }

    public static void pulish(Object source){
        pulish(null, source);
    }

    public static void pulish(Object mark, Object source){
        if (source == null){
            throw new IllegalStateException("source is not allow null");
        }
        Class<?> primordialClass = BeanUtil.getPrimordialClass(source);
        synchronized (typeMatchingListenerCache){
            for (Class<?> type : typeMatchingListenerCache.keySet()) {
                if (type.isAssignableFrom(primordialClass)){
                    List<TypeMatchingListener<Object>> listeners = typeMatchingListenerCache.get(type);
                    for (TypeMatchingListener<Object> listener : listeners) {
                        try {

                            listener.handlerEvent(source);
                        } catch (Throwable e) {
                            CentralizedExceptionHandling.handlerException(e);
                        }
                    }
                }
            }
        }
        if (mark != null){
            for (MarkMatchingListener<Object> listener : markMatchingListenerCache) {
                if (listener.matching(mark)) {
                    try {

                        listener.handlerEvent(source);
                    } catch (Throwable e) {
                        CentralizedExceptionHandling.handlerException(e);
                    }
                }
            }
        }
    }


}
