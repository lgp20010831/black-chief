package com.black.callback.develop;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.callback.ApplicationStartingTaskManager;
import com.black.core.annotation.Sort;
import com.black.core.cache.ClassSourceCache;
import com.black.core.chain.Order;
import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.tools.BeanUtil;
import com.black.core.util.ClassUtils;
import com.black.core.util.StreamUtils;
import com.black.pattern.Premise;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.utils.ServiceUtils;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author 李桂鹏
 * @create 2023-05-17 13:52
 */
@SuppressWarnings("all") @Sort(200)
public class DevelopLaunchPlugin implements SpringApplicationRunListener {


    private static DevelopLaunchPlugin launchPlugin;

    public synchronized static DevelopLaunchPlugin getInstance() {
        if (launchPlugin == null){
            launchPlugin = new DevelopLaunchPlugin();
        }
        return launchPlugin;
    }

    private DevelopmentContext developmentContext;

    public DevelopLaunchPlugin(){
        launchPlugin = this;
        developmentContext = new DefaultDevelopmentContext();

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        Set<Class<?>> mainClasses = ChiefApplicationRunner.getMainClasses();
        ChiefScanner scanner = ScannerManager.getScanner();
        List<Class<? extends Developer>> developerEarlyClasses = new ArrayList<>();
        for (Class<?> mainClass : mainClasses) {
            String packageName = ClassUtils.getPackageName(mainClass);
            Set<Class<?>> source = ClassSourceCache.getSource(packageName);
            if (source == null){
                Set<Class<?>> classes = scanner.load(packageName);
                ClassSourceCache.registerSource(packageName, classes);
            }
            developerEarlyClasses.addAll(filterDevelopers(source));
        }

        List<Developer> developers = instanceDevelopers(developerEarlyClasses);
        developers = sortDeveloper(developers);
        for (Developer developer : developers) {
            pullEnabledDeveloper(developer);
        }

        for (Developer developer : developers) {
            developer.prepare();
        }

        for (Developer developer : developers) {
            developmentContext.registerDeveloper(developer);
        }

        ApplicationStartingTaskManager.addBatchTask(classes -> {
            developmentContext.postSources(classes);
        });
    }

    private <T extends Annotation> void pullEnabledDeveloper(Developer developer){
        if (developer instanceof EnabledAnnotationAware){
            Class<Developer> primordialClass = BeanUtil.getPrimordialClass(developer);
            EnabledByAnnotation annotation = primordialClass.getAnnotation(EnabledByAnnotation.class);
            if (annotation != null){
                T target = (T) ChiefApplicationRunner.getAnnotation(annotation.value());
                if (target != null){
                    ((EnabledAnnotationAware<T>) developer).pullAnnotation(target);
                }
            }
        }

    }

    private List<Developer> sortDeveloper(List<Developer> developers){
        return ServiceUtils.sort(developers, developer -> {
            if (developer instanceof Order){
                return ((Order) developer).getOrder();
            }
            Class<Developer> primordialClass = BeanUtil.getPrimordialClass(developer);
            Sort annotation = primordialClass.getAnnotation(Sort.class);
            return annotation == null ? 0 : annotation.value();
        }, false);
    }

    private List<Developer> instanceDevelopers(List<Class<? extends Developer>> classes){
        BeanFactory beanFactory = FactoryManager.initAndGetBeanFactory();
        return StreamUtils.mapList(classes, type -> {
            return beanFactory.getSingleBean(type);
        });
    }

    private List<Class<? extends Developer>> filterDevelopers(Set<Class<?>> classes){
        Object source = StreamUtils.filterList(classes, clazz -> {
            Class<?> type = clazz;
            if (BeanUtil.isSolidClass(type) && Developer.class.isAssignableFrom(type)){
                EnabledByAnnotation annotation = type.getAnnotation(EnabledByAnnotation.class);
                if (annotation != null){
                    if (!ChiefApplicationRunner.isPertain(annotation.value())){
                        return false;
                    }
                }
                EnabledByCustom custom = type.getAnnotation(EnabledByCustom.class);
                if (custom != null){
                    Premise premise = InstanceBeanManager.instance(custom.value(), InstanceType.REFLEX_AND_BEAN_FACTORY);
                    if (!premise.premise()){
                        return false;
                    }
                }
                return true;
            }

            return false;
        });
        return (List<Class<? extends Developer>>) source;
    }


    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        developmentContext.prepareLoad();
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        developmentContext.started();
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        developmentContext.running();
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        developmentContext.failed();
    }
}
