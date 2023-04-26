package com.black.core.factory;

import com.black.core.json.NotNull;
import com.black.core.spring.instance.ClassTypeGenericConvertedException;

import java.util.*;

public class ClassMappingFactory extends AbstractMutesFactory<Class<?>, Class<?>> {

    private final Map<Class<?>, Class<?>> cache = new HashMap<>();


    @Override
    protected void doConvert(Map<Class<?>, Collection<Class<?>>> targetSource) {

    }

    @Override
    protected void doMerge(Map<Class<?>, Collection<Class<?>>> targetSource) {

    }

    @Override
    public Collection<Class<?>> getBean(Class<?> key) {
        return null;
    }

    @Override
    public Collection<Class<?>> getMutes(Class<?> key) {
        return source.get(key);
    }

    public static <F> ArrayList<Class<? super F>> getSuperList(@NotNull Class<F> targetClazz){
        final Class<?> topClazz = Object.class;
        ArrayList<Class<? super F>> result = new ArrayList<>();
        Class<? super F> superclass = targetClazz.getSuperclass();
        for (;;){
            if(topClazz.equals(superclass)){
                return result;
            }
            result.add(superclass);
            superclass = superclass.getSuperclass();
        }
    }

    public static <T> boolean removeSuperAndJudgeHasEnhanceSon(Collection<Class<? extends T>> source, Class<?> targetClazz){
        source.removeIf(key -> key.equals(targetClazz) || key.isAssignableFrom(targetClazz));
        for (Class<?> presentClazz : source) {
            if (targetClazz.isAssignableFrom(presentClazz)) {
                return false;
            }
        }
        return true;
    }

    public static <I> Collection<Class<?>> getLoopInterfaces(Class<I> targetInterface){
        if (targetInterface == null || !targetInterface.isInterface()){
            throw new RuntimeException("参数必须不能为空,且是一个接口:" + targetInterface);
        }
        Collection<Class<?>> result = new ArrayList<>();
        Class<?>[] interfaces = targetInterface.getInterfaces();
        if (interfaces.length != 0){
            for (Class<?> anInterface : interfaces) {
                result.add(anInterface);
                result.addAll(getLoopInterfaces(anInterface));
            }
        }
        return result;
    }

    public static Collection<Class<?>> getInterface(Class<?> targetClazz){
        Collection<Class<?>> interfaceCollect = new ArrayList<>();
        for (Class<?> anInterface : targetClazz.getInterfaces()) {
            interfaceCollect.add(anInterface);
            interfaceCollect.addAll(getLoopInterfaces(anInterface));
        }
        return interfaceCollect;
    }

    public static <V> Class<? super V> typeChange(Class<?> type){
        return (Class<? super V>) type;
    }

    public static <V> Class<? extends V> typeJudge(Class<?> type, Class<V> targetClazz){
        if (targetClazz.isAssignableFrom(type)){
            return (Class<? extends V>) type;
        }
        throw new ClassTypeGenericConvertedException(targetClazz, type);
    }

    public <K, V extends K> void registerMapping(Class<K> key, V value){
        final Class<V> reallyClazz = (Class<V>) value.getClass();
        final ArrayList<Class<? super V>> superList = getSuperList(reallyClazz);
        superList.add(reallyClazz);
        Collection<Class<?>> anInterface = getInterface(reallyClazz);
        for (Class<?> interfaceClazz : anInterface) {
            register(interfaceClazz, key);
        }
        for (Class<? super V> s : superList) {
            for (Class<?> clazz : getInterface(s)) {
                register(clazz, key);
            }
            register(s, key);
        }
    }

    public void register(Class<?> targetClass, Class<?> key){
        Collection<Class<?>> saveList = source.computeIfAbsent(targetClass, k -> new ArrayList<>());
        if (!saveList.contains(key)){
            saveList.add(key);
        }
    }
    public <Q> Class<?> querySinletonKey(@NotNull Class<Q> condition){
        Collection<Class<?>> mappingKeys = getMappingKeys(condition);
        if (!mappingKeys.isEmpty()) {
            for (Class<?> mappingKey : mappingKeys) {
                return mappingKey;
            }
        }
        return null;
    }

    public <Q> Collection<Class<?>> getMappingKeys(@NotNull Class<Q> condition){
        Collection<Class<?>> list = source.get(condition);
        if (list == null){
            return new ArrayList<>();
        }
        return list;
    }

    public static <F> List<Class<? extends F>> obtainSuperClazz(Class<? super F> condition, Collection<Class<?>> source){
        List<Class<? extends F>> result = new ArrayList<>();
        for (Class<?> target : source) {
            if (condition.isAssignableFrom(target)){
                result.add((Class<? extends F>) target);
            }
        }
        return result;
    }

    public Class<?> hitClassSpectrum(Class<?> targetClass, Collection<Class<?>> keySet){
        return hitClassSpectrum(targetClass, keySet, null);
    }

    public Class<?> hitClassSpectrum(Class<?> targetClass, Collection<Class<?>> keySet,
                                     Map<Class<?>, Object> source){

        if (targetClass == null || keySet == null)
            return null;
        if (cache.containsKey(targetClass)){
            return cache.get(targetClass);
        }
        Class<?> key = null;
        try {
            if (keySet instanceof HashSet){
                Class<?> c = targetClass;
                do {
                    if (keySet.contains(c)) {
                        key = c;
                        return key;
                    }
                    c = c.getSuperclass();
                } while (c != null && !c.equals(Object.class));
                for (Class<?> anInterface : targetClass.getInterfaces()) {
                    if (keySet.contains(anInterface)){
                        key = anInterface;
                        return key;
                    }
                }
            }else {

                for (Class<?> clazz : keySet) {
                    if (clazz.equals(targetClass)){
                        key = clazz;
                        return key;
                    }
                }
                for (Class<?> clazz : keySet) {
                    if (source != null){
                        final Class<?> valueClazz = source.get(clazz).getClass();
                        if (targetClass.isAssignableFrom(valueClazz)){
                            key = clazz;
                            return key;
                        }
                    }

                    if (targetClass.isAssignableFrom(clazz)) {
                        key = clazz;
                        return key;
                    }
                }
            }
        }finally {
            if (key != null){
                cache.put(targetClass, key);
            }
        }
        return key;
    }
}
