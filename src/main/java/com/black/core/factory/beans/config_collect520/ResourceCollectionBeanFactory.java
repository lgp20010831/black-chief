package com.black.core.factory.beans.config_collect520;

import com.black.core.Beacon;
import com.black.core.cache.ClassSourceCache;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.tools.BeanUtil;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.ClassUtils;
import com.black.scan.ChiefScanner;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.annotation.Annotation;
import java.util.*;

@SuppressWarnings("all") @Log4j2
public class ResourceCollectionBeanFactory extends AttributeInjectionEnhancementBeanFactory{

    private Class<?> coordinate;

    public ResourceCollectionBeanFactory() {
        this(null);
    }

    public ResourceCollectionBeanFactory(DefaultListableBeanFactory springFactory) {
        super(springFactory);
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        setCoordinate(mainClass);
        registerBeanFactoryProcessor(new CollectFieldHandler());
        registerBeanFactoryProcessor(new CollectParamHandler());
    }

    public void setCoordinate(Class<?> coordinate) {
        this.coordinate = coordinate;
    }

    public Class<?> getCoordinate() {
        return coordinate;
    }

    public String getDefaultScope(){
        Class<?> coordinate = getCoordinate();
        if (coordinate == null){
            setCoordinate(Beacon.class);
            coordinate = Beacon.class;
        }
        return ClassUtils.getPackageName(coordinate);
    }

    public List<Object> collect(CollectCondition collectCondition){
        String[] scope = collectCondition.getScope();
        Set<String> ranges;
        if (scope == null || scope.length == 0){
            ranges = Collections.singleton(getDefaultScope());
        }else {
            ranges = new LinkedHashSet<>(Arrays.asList(scope));
        }
        Set<Class<?>> sources = new HashSet<>();
        ChiefScanner scanner = getLoader();
        for (String range : ranges) {
            Set<Class<?>> classes = ClassSourceCache.getSource(range);
            if (classes == null){
                classes = scanner.load(range);
                ClassSourceCache.registerSource(range, classes);
            }
            sources.addAll(classes);
        }

        List<Object> list = new ArrayList<>();
        loop: for (Class<?> source : sources) {
            if (collectCondition.isSoild()) {
                if (!BeanUtil.isSolidClass(source)){
                    continue loop;
                }
            }

            Class<? extends ClassMatchCustom>[] customs = collectCondition.getCustoms();
            if (customs != null && customs.length > 0){
                boolean match = false;
                for (Class<? extends ClassMatchCustom> custom : customs) {
                    ClassMatchCustom matchCustom = getSingleBean(custom);
                    if (matchCustom.match(source)){
                        match = true;
                        break;
                    }
                }
                if (!match){
                    continue loop;
                }
            }else {
                //match type
                Class<?>[] types = collectCondition.getType();
                if (types != null && types.length > 0){
                    boolean typeOr = collectCondition.isTypeOr();
                    boolean conform = typeOr ? false : true;
                    for (Class<?> type : types) {
                        if (type.isAssignableFrom(source)){
                            if (typeOr){
                                conform = true;
                            }
                            break;
                        }else {
                            if (!typeOr){
                                conform = false;
                                break;
                            }
                        }
                    }

                    if (!conform){
                        continue loop;
                    }
                }

                //match annotation
                Class<? extends Annotation>[] annotationAt = collectCondition.getAnnotationAt();
                if (annotationAt != null && annotationAt.length > 0){
                    boolean isAnnotationOr = collectCondition.isAnnotationOr();
                    boolean conform = isAnnotationOr ? false : true;
                    for (Class<? extends Annotation> type : annotationAt) {
                        if (AnnotationUtils.isPertain(source, type)){
                            if (isAnnotationOr){
                                conform = true;
                            }
                            break;
                        }else {
                            if (!isAnnotationOr){
                                conform = false;
                                break;
                            }
                        }
                    }

                    if (!conform){
                        continue loop;
                    }
                }
            }

            //condition pass
            if (collectCondition.isInstance()){
                try {
                    Object bean = collectCondition.isPrototypeCreate() ? prototypeCreateBean(source) : getSingleBean(source);
                    list.add(bean);
                }catch (RuntimeException e){
                    if (collectCondition.isAbandonUnableInstance()){
                        log.info("discard collection elements that cannot be instantiated:{} by {}",
                                source, ServiceUtils.getThrowableMessage(e));
                        continue loop;
                    }else {
                        throw e;
                    }
                }
            }else {
                list.add(source);
            }
        }

        if (collectCondition.isSingle()){
            if (list.size() > 1){
                throw new IllegalStateException("I hope to obtain only one matching element, " +
                        "but I have collected multiple");
            }
        }

        return list;
    }
}
