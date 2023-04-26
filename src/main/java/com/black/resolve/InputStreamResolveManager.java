package com.black.resolve;

import com.black.io.in.JHexByteArrayInputStream;
import com.black.resolve.annotation.ResolveSort;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.tools.BeanUtil;
import com.black.utils.LocalList;
import com.black.utils.ServiceUtils;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings("all")
public class InputStreamResolveManager {

    //是否保证并发安全
    private static volatile boolean concurrentSecurity = false;

    private static final LinkedBlockingQueue<Class<? extends InputStreamResolver>> resolverTypeQueue = new LinkedBlockingQueue<>();

    private static List<InputStreamResolver> resolvers;

    private static LocalList<InputStreamResolver> concurrentResolvers;

    static {
        ChiefScanner scanner = ScannerManager.getScanner();
        Set<Class<?>> sources = scanner.load("com.black.resolve.impl");
        for (Class<?> source : sources) {
            if (BeanUtil.isSolidClass(source) && InputStreamResolver.class.isAssignableFrom(source)){
                registerType((Class<? extends InputStreamResolver>) source);
            }
        }
    }

    public static boolean isConcurrentSecurity() {
        return concurrentSecurity;
    }

    public static void setConcurrentSecurity(boolean concurrentSecurity) {
        InputStreamResolveManager.concurrentSecurity = concurrentSecurity;
    }

    public static void registerType(@NonNull Class<? extends InputStreamResolver> resolveType){
        resolverTypeQueue.add(resolveType);
    }

    private static Collection<InputStreamResolver> instanceResolveQueue(){
        if (isConcurrentSecurity()){
            if (concurrentResolvers == null){
                concurrentResolvers = new LocalList<>();
                concurrentResolvers.addAll(instanceResolveQueue0());
            }
            return concurrentResolvers.current();
        }else {
            if (resolvers == null){
                resolvers = new ArrayList<>();
                resolvers.addAll(instanceResolveQueue0());
            }
            return resolvers;
        }
    }

    private static List<InputStreamResolver> instanceResolveQueue0(){
        BeanFactory factory = FactoryManager.initAndGetBeanFactory();
        ArrayList<InputStreamResolver> list = new ArrayList<>();
        for (Class<? extends InputStreamResolver> type : resolverTypeQueue) {
            InputStreamResolver resolver = factory.getSingleBean(type);
            list.add(resolver);
        }
        return ServiceUtils.sort(list, bean -> {
            Class<?> primordialClass = BeanUtil.getPrimordialClass(bean);
            ResolveSort annotation = primordialClass.getAnnotation(ResolveSort.class);
            return annotation == null ? 0 : annotation.value();
        }, true);
    }

    public static Object resolve(JHexByteArrayInputStream inputStream, @NonNull Object rack){
        Collection<InputStreamResolver> resolvers = instanceResolveQueue();
        for (InputStreamResolver resolver : resolvers) {
            if (resolver.support(rack)) {
                try {
                    return resolver.doResolve(rack, inputStream);
                } catch (Throwable e) {
                    throw new ResolveException(e);
                }
            }
        }
        return null;
    }

}
