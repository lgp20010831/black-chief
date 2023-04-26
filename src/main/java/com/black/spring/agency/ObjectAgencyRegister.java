package com.black.spring.agency;

import com.black.bin.ApplyProxyFactory;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.pattern.MethodInvoker;
import com.black.core.annotation.Sort;
import com.black.core.chain.GroupKeys;
import com.black.core.tools.BeanUtil;
import com.black.utils.ServiceUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Setter @Getter
public class ObjectAgencyRegister {

    private static ObjectAgencyRegister register;

    public synchronized static ObjectAgencyRegister getInstance() {
        if (register == null){
            register = new ObjectAgencyRegister();
        }
        return register;
    }

    private ObjectAgencyRegister(){

    }

    private final Map<TemporaryDecisionsMethodAgency, MethodInvoker> decisionsMethodAgencyMethodInvokerMap = new ConcurrentHashMap<>();

    private final Map<GroupKeys, List<MethodInvoker>> cache = new ConcurrentHashMap<>();

    private final Set<Class<?>> supportClasses = new HashSet<>();

    private Set<String> proxyRange = new HashSet<>();

    private boolean proxyAll = false;

    public void addProxyRange(String range){
        proxyRange.add(range);
    }

    public boolean inRange(Object instance){
        if (proxyRange.isEmpty()){
            return true;
        }
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(instance);
        for (String range : proxyRange) {
            if (primordialClass.getName().startsWith(range)) {
                return true;
            }
        }
        return false;
    }

    public void setProxyAll(boolean proxyAll) {
        this.proxyAll = proxyAll;
    }

    public boolean isProxyAll() {
        return proxyAll;
    }

    public Map<GroupKeys, List<MethodInvoker>> getCache() {
        return cache;
    }

    public Map<TemporaryDecisionsMethodAgency, MethodInvoker> getDecisionsMethodAgencyMethodInvokerMap() {
        return decisionsMethodAgencyMethodInvokerMap;
    }

    public List<MethodInvoker> getMethodInvoker(Class<?> type, Method method){
        GroupKeys groupKeys = new GroupKeys(type, method);
        List<MethodInvoker> directInvokes = cache.get(groupKeys);
        List<MethodInvoker> temp = new ArrayList<>();
        if (directInvokes != null){
            temp.addAll(directInvokes);
        }
        for (TemporaryDecisionsMethodAgency methodAgency : decisionsMethodAgencyMethodInvokerMap.keySet()) {
            MethodInvoker invoker = decisionsMethodAgencyMethodInvokerMap.get(methodAgency);
            if (methodAgency.capable(method)) {
                temp.add(invoker);
            }
        }
        sort(temp);
        return temp.isEmpty() ? null : temp;
    }

    public void register(@NonNull GroupKeys target, @NonNull MethodInvoker invoker){
        List<MethodInvoker> methodInvokers = cache.computeIfAbsent(target, t -> new ArrayList<>());
        methodInvokers.add(invoker);
    }

    public void registerFunction(TemporaryDecisionsMethodAgency decisionsMethodAgency, MethodInvoker invoker){
        decisionsMethodAgencyMethodInvokerMap.put(decisionsMethodAgency, invoker);
    }

    private void sort(List<MethodInvoker> methodInvokers){
        ServiceUtils.sort(methodInvokers, methodInvoker -> {
            Sort annotation = methodInvoker.getAnnotation(Sort.class);
            return annotation == null ? 0 : annotation.value();
        }, false);
    }

    public boolean isSupport(Object target){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(target);
        if (supportClasses.contains(primordialClass)) {
            return true;
        }
        for (Class<?> supportClass : supportClasses) {
            if (supportClass.isAssignableFrom(primordialClass)){
                return true;
            }
        }
        return false;
    }

    public void registerSupport(@NonNull Class<?> type){
        if (!Object.class.equals(type)){
            supportClasses.add(type);
        }
    }


    public boolean isCglibProxy(Object instance){
        return ApplyProxyFactory.isCanCglibProxyOrRegular(instance, true);
    }

    public Object proxyInstance(@NonNull Object instance){
        return ApplyProxyFactory.proxy(instance, new AgencyRewriteLayer(), true);
    }

    public Object proxyAndInstance(@NonNull Class<?> type){
        Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
        return proxyInstance(instance);
    }

}
