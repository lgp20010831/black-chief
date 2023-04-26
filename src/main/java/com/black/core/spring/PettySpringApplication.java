package com.black.core.spring;

import com.black.scan.ChiefScanner;
import com.black.core.Beacon;
import com.black.core.cache.ClassSourceCache;
import com.black.core.json.NotNull;
import com.black.core.util.SimplePattern;
import com.black.core.spring.component.DefaultLoadDriver;
import com.black.core.spring.driver.*;
import com.black.core.spring.event.ApplicationEvent;
import com.black.core.spring.pureness.DefaultChiefApplicationConfigurer;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.util.Av0;
import com.black.core.util.ClassUtils;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;




@Log4j2
public class PettySpringApplication extends AbstractDriverCache {

    /***
     *
     * 加载流程
     * init constructor()
     *       ↓
     *              事件: create:
     *     initFactory 初始化工厂, 并将自身注册到工厂中
     *
     *              事件: pattern:
     *     ↓ 下一个执行点
     *     {@link BeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry(BeanDefinitionRegistry)}
     *       ↓ →  initLoad 初始化组件,准备通过{@link SimplePattern} 开始扫描整个工程, 注册了 defaultPostPatternClazzDriver
     *            和{@link ComponentScanner}
     *              ↓
     *           通过loadPatternDriver  遍历工程内的所有 class 对象,  获取所有实现了{@link PostPatternClazzDriver} 的实例
     *           收集完了所有的 PostPatternDriver 组件以后, 循环遍历组件集合
     *           首先执行的是  defaultPostPatternClazzDriver 扫描所有 class,并将实现了springLoadComponent 的实例构建出来保存到缓存中
     *           同时加载工程中的所有配置类
     *       ↓ 下一个执行点
     *      postProcessBeanFactory
     *      在这个执行点中并没有做任何事
     *
     *      ↓ 下一个执行点 onApplicationEvent
     *              事件: load
     *
     *      整理收集到的所有 springLoadComponent 组件,并与spring容器里的bean过滤筛选
     *      然后 排序
     *      遍历所有组件调用 load
     *
     *
     *      12.21 修改:
     *      petty基本属性通过新的配置提取类来控制
     *      取消pettyApplication 作为配置类
     *      Component 的实例化推迟
     *      在扫描 clazz 中收集 component 的 class 对象, 然后统一实例化
     *      只有实现了{@link PostPatternClazzDriver} 会在被扫描到以后立即实例化
     *      重写代理工厂, 只代理一次,但是可以多次执行
     *      将获取扫描的包单独拎出来
     *      统一添加一个接口, 注册一个注解,然后注解存在实体类上,则加载这个组件, 如果没有实现则默认加载
     *      统一打印日志
     */

    /**
     * 早期扫描到的实现了 组件接口的类
     */
    private final Collection<Class<? extends OpenComponent>> earlyComponentClazzList = new ArrayList<>();

    /***
     * 最终保存组件的缓存
     */
    private final Collection<Object> loadCache = new ArrayList<>(8);

    /***
     * 存贮当前项目下所有的配置文件
     */
    private final Collection<Object> applicationConfigurationMutes = new HashSet<>(4);


    /**
     * spring 扩展加载组建
     * 扫描所有项目中实现 SpringLoadComponent 接口的类,把他们初始化并且
     * 加载到缓存中,当 spring 容器加载完成后 调用其 load 方法执行
     */
    private final Map<Class<? extends OpenComponent>, Object> springLoadComponent = new ConcurrentHashMap<>(8);


    public PettySpringApplication(){
        this(new DefaultChiefApplicationConfigurer());
    }

    public PettySpringApplication(Object configuration){
        super(configuration);
        if (configuration != null){
            applicationConfigurationMutes.add(configuration);
        }
        ChiefApplicationHolder.expansivelyApplication = this;
    }

    public void distory(){
        log.info("distory appliaction");
        projectClasses.clear();
        applicationConfigurationMutes.clear();
        loadCache.clear();
        earlyComponentClazzList.clear();
        springLoadComponent.clear();
        refuseComponents.clear();
        componentCache.clear();
        adjustmentEventWithDistory();
    }

    public void load(){
        if (getEvent() == ApplicationEvent.distory){
            throw new IllegalStateException("current application is distory");
        }
        doOnApplicationEvent();
    }

    final String[] sortScanPackages(String[] scanPackages){
        if (scanPackages == null || scanPackages.length == 0){
            return new String[0];
        }
        Set<String> set = Av0.set(scanPackages);
        List<String> resultList = new ArrayList<>();
        resultList.add(ClassUtils.getPackageName(Beacon.class));
        for (String df : set) {
            if (!resultList.contains(df)){
                resultList.add(df);
            }
        }
        return resultList.toArray(new String[0]);
    }

    @Override
    public void scanMutes() {
        String[] scanPackages = sortScanPackages(applicationConfiguration.getScanPackages());
        if (log.isInfoEnabled()) {
            log.info("chiefExpansivelyApplication scan range: {}", Arrays.toString(scanPackages));
        }
        Collection<PostPatternClazzDriver> patternClazzDrivers = postPatternClazzDriverMutes();
        ChiefScanner chiefScanner = obtainScanner();
        if (scanPackages != null && chiefScanner != null){
            initPatternDrivers();
            Collection<PostPatternClazzDriver> doPatterns;
            for (String scanPackage : scanPackages) {
                doPatterns = ApplicationUtil.clone(patternClazzDrivers);

                Set<Class<?>> source;
                source = ClassSourceCache.getSource(scanPackage);
                if (source == null){
                    source = chiefScanner.load(scanPackage);
                    ClassSourceCache.registerSource(scanPackage, source);
                    log.info("[chief application] use scanner:{}, scan path:{}, get source:{}",
                            chiefScanner, scanPackage, source.size());
                }
                handlerScanElement(source, doPatterns);
            }
            doPatterns = ApplicationUtil.clone(patternClazzDrivers);
            handlerScanElement(applicationConfiguration.getRegisterComponentMutes(), doPatterns);
        }
        scanApplicationConfig(scanPackages, patternClazzDrivers);

        //finally end pattren
        for (PostPatternClazzDriver pattern : postPatternClazzDriverMutes()) {
            pattern.finallyEndPattern(projectClasses, springLoadComponent, reusingProxyFactory, this);
        }
    }

    private void handlerScanElement(Collection<Class<?>> source, Collection<PostPatternClazzDriver> patternDrivers){
        for (Class<?> clazz : source) {
            for (PostPatternClazzDriver postPatternClazzDriver : patternDrivers) {
                postPatternClazzDriver.postPatternClazz(clazz, springLoadComponent,
                        reusingProxyFactory, this);
            }
        }
        if (source != null && !source.isEmpty()){
            for (PostPatternClazzDriver patternDriver : patternDrivers) {
                patternDriver.endPattern(source, springLoadComponent,
                        reusingProxyFactory, this);
            }
        }
        projectClasses.addAll(source);
    }


    protected void scanApplicationConfig(String[] packages, Collection<PostPatternClazzDriver> patternDrivers){
        applicationConfiguration.againReadConfig(this);
        String[] scanPackages = applicationConfiguration.getScanPackages();
        Collection<String> scanPages = filterScanPages(packages, scanPackages);
        ChiefScanner chiefScanner = obtainScanner();
        for (String scanPackage : scanPages) {
            final Collection<PostPatternClazzDriver> doPatterns = ApplicationUtil.clone(patternDrivers);
            Set<Class<?>> source;
            source = ClassSourceCache.getSource(scanPackage);
            if (source == null){
                source = chiefScanner.load(scanPackage);
                ClassSourceCache.registerSource(scanPackage, source);
                log.info("[chief application] use scanner:{}, scan path:{}, get source:{}",
                        chiefScanner, scanPackage, source.size());
            }
            handlerScanElement(source, doPatterns);
        }
    }


    protected Collection<String> filterScanPages(String[] alreayScan, String[] newScanPackages){
        if (newScanPackages.length < alreayScan.length){
            if (log.isDebugEnabled()) {
                log.debug("扫描范围异常的丢失");
            }
            return null;
        }
        Collection<String> sc = new HashSet<>();
        alreay: for (String newScanPackage : newScanPackages) {
            for (String as : alreayScan) {
                if (as.equals(newScanPackage)){
                    continue alreay;
                }
            }
            sc.add(newScanPackage);
        }
        return sc;
    }

    @Override
    public void instanceAllElment() {
        driverInitializationCache.forEach((k, v) ->{
            Collection<Driver> drivers = new ArrayList<>();
            for (Class<? extends Driver> driverClazz : v) {
                Object instance = instanceComponent(driverClazz);
                if (instance != null){
                    drivers.add((Driver) instance);
                }
            }
            Collection<Driver> createdDrivers = driverInstanceCache.computeIfAbsent(k, key -> new ArrayList<>());
            createdDrivers.addAll(drivers);
        });

        for (Class<? extends OpenComponent> componentClazz : earlyComponentClazzList) {
            registerComponent(componentClazz);
        }
    }

    private void initPatternDrivers(){
        Collection<PostPatternClazzDriver> postPatternClazzDrivers = postPatternClazzDriverMutes();
        for (PostPatternClazzDriver postPatternClazzDriver : postPatternClazzDrivers) {
            if (ComponentScanner.class.isAssignableFrom(postPatternClazzDriver.getClass())){
                return;
            }
        }
        postPatternClazzDrivers.add(createInstance(ComponentScanner.class));
    }

    public Collection<Class<? extends OpenComponent>> obtainEarlyComponentClazzList() {
        return earlyComponentClazzList;
    }

    @Override
    public Map<Class<? extends OpenComponent>, Object> obtainOpenComponents() {
        return springLoadComponent;
    }

    //****************************  get **************************


    public Collection<Object> getApplicationConfigurationMutes() {
        return applicationConfigurationMutes;
    }

    /***
     * 查询已经注册的组件, 根据 class 类型查询
     * @param componentType 组件类型, 同一继承关系的组件只能存在一个
     * @param <T> type
     * @return 返回组件对象, 如果不存在则返回空
     */
    public <T extends OpenComponent> T queryComponent(@NotNull Class<T> componentType){
        for (Class<? extends OpenComponent> keyClass : springLoadComponent.keySet()) {

            if (componentType.isAssignableFrom(keyClass))
                return (T) springLoadComponent.get(keyClass);
        }
        return null;
    }


    /* 将组件排序 */
    public void sort(){
        if (driverInstanceCache.containsKey(SortDriver.class)){
            Collection<SortDriver> sortDrivers = sortDrivers();
            if (!sortDrivers.isEmpty()){
                for (SortDriver sortDriver : sortDrivers) {
                    sortDriver.sort(loadCache, springLoadComponent, this);
                }
            }
        }
    }

    protected Map<String, Object> getSpringMutes(){
        return new HashMap<>();
    }

    @Override
    public void doOnApplicationEvent() {
        if (loaded){
            log.info("the current application has been loaded");
            return;
        }
        try {
            Map<String, Object> springMutes = getSpringMutes();
            /* 加载 spring 缓存中的所有 bean */
            if (driverInstanceCache.containsKey(PostSpringMutesDriver.class)){
                Collection<PostSpringMutesDriver> postSpringMutesDrivers = postSpringMutesDrivers();
                if (!postSpringMutesDrivers.isEmpty()){
                    for (PostSpringMutesDriver postSpringMutesDriver : postSpringMutesDrivers) {
                        postSpringMutesDriver.processorMutes(springMutes,
                                springLoadComponent, this);
                    }
                }
            }

            //三块缓存区: loadMutes, springMutes, springLoadComponent
            if (driverInstanceCache.containsKey(PostLoadBeanMutesDriver.class)){
                Collection<PostLoadBeanMutesDriver> postLoadBeanMutesDrivers = postLoadBeanMutesDrivers();
                if (!postLoadBeanMutesDrivers.isEmpty()){
                    for (PostLoadBeanMutesDriver postDriver : postLoadBeanMutesDrivers) {
                        postDriver.postLoadBeanMutes(instanceFactory.getMutes(), springMutes, springLoadComponent, this);
                    }
                }
            }

            /* 对所有组件进行排序 */
            sort();
            if (!loadCache.isEmpty()){
                if (applicationConfiguration.isPrintComponentLoadLog()){
                    if (log.isInfoEnabled() && !DefaultLoadDriver.isCancel()) {
                        log.info("starting part components...");
                    }
                }
                if (driverInstanceCache.containsKey(LoadComponentDriver.class)){
                    Collection<LoadComponentDriver> loadComponentDrivers = loadComponentDrivers();
                    if (!loadComponentDrivers.isEmpty()){
                        for (LoadComponentDriver postDriver : loadComponentDrivers) {
                            postDriver.load(loadCache, this);
                        }
                    }
                }
                if (applicationConfiguration.isPrintComponentLoadLog()){
                    if (log.isInfoEnabled() && !DefaultLoadDriver.isCancel()) {
                        log.info("invoke components finish...");
                    }
                }
            }else {
                if (applicationConfiguration.isPrintComponentLoadLog()){
                    if (log.isInfoEnabled()) {
                        log.info("no component...");
                    }
                }
            }
        }finally {
            //projectClasses.clear();
            setLoaded(true);
        }
    }

    @Override
    protected void pareLoad() {
    }
}