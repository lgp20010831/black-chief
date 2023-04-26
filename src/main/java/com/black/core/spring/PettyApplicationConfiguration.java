package com.black.core.spring;

import com.black.core.spring.pureness.DefaultChiefApplicationConfigurer;
import lombok.extern.log4j.Log4j2;

import java.util.*;


@Log4j2
public class PettyApplicationConfiguration {


    protected final ChiefExpansivelyApplication chiefExpansivelyApplication;
    /***
     * 扫描的包, 如果扫描出多个启动类
     * 需要 springBootStartUpClazz 来纠正
     */
    private Set<String> scanPackages = new HashSet<>();

    //所依赖的配置类
    private Object relyOnConfiguration;

    /**
     * 中止{@link ChiefExpansivelyApplication}的加载
     */
    private boolean cancelLoad;

    /***
     * 指定当前项目中springBoot 启动类是哪个
     * 该值只有在 scan 扫描过程中发现多个启动类时
     * 才会生效
     */
    private Class<?> springBootStartUpClazz;

    /**
     * 打印组件执行 load 时的日志
     */
    private boolean printComponentLoadLog;



    private final ChooseScanRangeHolder scanRangeHolder;

    private final Collection<Class<?>> registerComponentMutes = new HashSet<>();

    public PettyApplicationConfiguration(ChooseScanRangeHolder chooseScanRangeHolder,
                                         ChiefExpansivelyApplication chiefExpansivelyApplication ) {
        this.scanRangeHolder = chooseScanRangeHolder;
        this.chiefExpansivelyApplication = chiefExpansivelyApplication;
    }

    public void init(){
        //如果依赖的配置类不是一个 ChiefApplicationConfigurer
        //则初始化一个默认的配置类
        if (relyOnConfiguration == null || !(relyOnConfiguration instanceof ChiefApplicationConfigurer)){
            initDefaultConfig();
        }else {
            handlerConfig((ChiefApplicationConfigurer) relyOnConfiguration);
        }

    }

    public void againReadConfig(ChiefExpansivelyApplication chiefExpansivelyApplication){
        Collection<Object> applicationConfigurationMutes = chiefExpansivelyApplication.getApplicationConfigurationMutes();
        if (applicationConfigurationMutes != null && !applicationConfigurationMutes.isEmpty()){
            for (Object applicationConfigurationMute : applicationConfigurationMutes) {
                if (!applicationConfigurationMute.equals(relyOnConfiguration)){
                    if (applicationConfigurationMute instanceof ChiefApplicationConfigurer){
                        ChiefApplicationConfigurer chiefApplicationConfigurer = (ChiefApplicationConfigurer) applicationConfigurationMute;
                        handlerConfig(chiefApplicationConfigurer);
                    }
                }
            }
        }
    }

    protected void handlerConfig(ChiefApplicationConfigurer chiefApplicationConfigurer){
        setScanPackages(chiefApplicationConfigurer.scanPackages());
        setCancelLoad(chiefApplicationConfigurer.cancelLoad());
        setPrintComponentLoadLog(chiefApplicationConfigurer.printLog());
        setSpringBootStartUpClazz(chiefApplicationConfigurer.pointSpringBootStartUpClazz());
        Collection<Class<?>> componentMutes = chiefApplicationConfigurer.registerComponentMutes();
        if (componentMutes != null && !componentMutes.isEmpty()){
            for (Class<?> componentMute : componentMutes) {
                registerComponentMutes.removeIf(m -> m.equals(componentMute) || m.isAssignableFrom(componentMute));
                registerComponentMutes.add(componentMute);
            }
        }
    }

    protected void initDefaultConfig(){
        handlerConfig(new DefaultChiefApplicationConfigurer());
    }

    public String[] getScanPackages() {
       return scanPackages.toArray(new String[0]);
    }

    public void setScanPackages(String[] scanPackages) {
        scanRangeHolder.screenRange(scanPackages);
        String[] obtainRanges = ChooseScanRangeHolder.obtainRanges();
        if (obtainRanges != null){
            this.scanPackages.addAll(new HashSet<>(Arrays.asList(obtainRanges)));
        }

    }


    public void setRelyOnConfiguration(Object relyOnConfiguration) {
        this.relyOnConfiguration = relyOnConfiguration;
    }

    public Object getRelyOnConfiguration() {
        return relyOnConfiguration;
    }

    public boolean isCancelLoad() {
        return cancelLoad;
    }

    public void setCancelLoad(boolean cancelLoad) {
        this.cancelLoad = cancelLoad;
    }

    public Class<?> getSpringBootStartUpClazz() {
        return springBootStartUpClazz;
    }

    public void setSpringBootStartUpClazz(Class<?> springBootStartUpClazz) {
        this.springBootStartUpClazz = springBootStartUpClazz;
    }

    public boolean isPrintComponentLoadLog() {
        return printComponentLoadLog;
    }

    public void setPrintComponentLoadLog(boolean printComponentLoadLog) {
        this.printComponentLoadLog = printComponentLoadLog;
    }

    public Collection<Class<?>> getRegisterComponentMutes() {
        return registerComponentMutes;
    }
}
