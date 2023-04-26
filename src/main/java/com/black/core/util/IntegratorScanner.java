package com.black.core.util;

import com.black.core.aop.listener.SpringEnvironmentHodler;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Log4j2
public class IntegratorScanner implements SimplePattern, ApplicationContextAware {
    public static boolean printLossMemory = false;
    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    ApplicationContext applicationContext;
    MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();
    ResourcePatternResolver resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(null);
    PropertyPlaceholderHelper helper;

    /** parser **/
    FilterEntryParser filterEntryParser = new FilterEntryParser();

    /** 针对线程安全问题 */
    volatile ResourceFileDo[] fileDo = new ResourceFileDo[2];

    private final String placeholderPrefix = SystemPropertyUtils.PLACEHOLDER_PREFIX;

    private final String placeholderSuffix = SystemPropertyUtils.PLACEHOLDER_SUFFIX;

    @Nullable
    private final String valueSeparator = SystemPropertyUtils.VALUE_SEPARATOR;


    /** 过滤条件 */
    String suffixFilter;

    /***
     * 当扫描器扫描到可读的资源文件时,调用 fileDo
     * @param fileDo 执行的逻辑,由于 {@link IntegratorScanner} 在 spring 中以单例模式存在
     *                         所以如果是自动注入到的扫描器，尽量不要在这里注入执行逻辑
     * @param suffixFilter 文件名后缀过滤器, 如果只想处理 class文件
     *                     则传递: .class, 如果想同时处理多个文件,则可以传递
     *                     [.class, .java, .graphql]
     *                     如果想处理除了 class 文件的其他文件,则需要
     *                     传递 ![.class], 同样过滤掉多个文件类型: ![.class, .java, .graphql]
     */
    @Override
    public void invokeWhenFile(ResourceFileDo fileDo, String suffixFilter) {

        if (this.fileDo[0] != null){

            //replace
            this.fileDo[1] = fileDo;
        }else
            this.fileDo[0] = fileDo;

        this.suffixFilter = suffixFilter;
    }

    /***
     * 加载 class 不传递任何参数, 所以需要子类将默认的扫描包获取出来
     * 默认为 spring 启动类所在的直接父包
     * @return 加载的 class， 中途很可能蹦掉
     */
    @Override
    public Set<Class<?>> loadClasses() {
        List<String> pages = AutoConfigurationPackages.get(applicationContext);
        return loadClasses(pages.get(0));
    }

    /***
     * 相比单参数的入口, 该方法更加的严谨, 他要求如果在加载 class过程中
     * 遇到错误(class not find)时, 应该怎么应对如果是单参数{@link SimplePattern#loadClasses(String)}
     * 会将异常向外抛出, 整个过程中止, 但是有了其他参数加入,可能就不会中止
     * @param packageName 包名
     * @param invokeWhenClassNotFind 当发生 class not find 异常时调用该方法,并将
     *                               当前处理的文件对象传递作为参数,如果该参数为空,则视为没有对
     *                               异常有任何处理，则会将异常向外抛出
     * @param breakWhenClassNotFind 当遇到 class not find 异常时是否安全中止
     *                              然后返回已经扫描加载到的 class set,此效果的前提是 invokeWhenClassNotFind
     *                              不为空, 他要求当前 class not find 的错误一定被处理之后,才能安全退出
     * @param fileDo 与 invokeWhenFile 不同在于，他将对象存在方法内存中,不受线程干扰, 优先级 > invokeWhenFile 传递的 fileDo
     *               如果该参数不为空, 那么调用该 fileDo 后不会再调用 invokeWhenFile 传递的 fileDo
     * @param suffixFilter 后缀过滤器, 与fileDo相同优先级大于 invokeWhenFile 的值
     * @return 加载的 class 对象, 不一定是理想下的全部,如果遇到错误, 之后的 class 可能不会被加载.
     */
    @Override
    public Set<Class<?>> loadClasses(String packageName, ResourceFileDo invokeWhenClassNotFind, boolean breakWhenClassNotFind,
                                     ResourceFileDo fileDo, String suffixFilter) {

        long freeMemory = Runtime.getRuntime().freeMemory()/1024;
        Set<Class<?>> set = new LinkedHashSet<>();

        // 此处固定写法即可,含义就是包及子包下的所有类
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(packageName) + '/' + DEFAULT_RESOURCE_PATTERN;

        try {
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {

                if (resource.isFile())
                    invokeDo(fileDo == null ? this.fileDo[0] : fileDo, suffixFilter == null ? this.suffixFilter : suffixFilter, resource);

                if (resource.isReadable()) {
                    MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                    String className = metadataReader.getClassMetadata().getClassName();

                    if (className == null)
                        continue;

                    Class<?> clazz;
                    try {
                        clazz = Class.forName(className);
                        set.add(clazz);
                    } catch (ClassNotFoundException e) {

                        if (invokeWhenClassNotFind == null)
                            throw new RuntimeException("error for create class:" + e);

                        invokeWhenClassNotFind.doHandler(resource.getFile());

                        if (breakWhenClassNotFind)
                            return set;
                    }catch (Throwable ex){

                        if (invokeWhenClassNotFind != null){
                            invokeWhenClassNotFind.doHandler(resource.getFile());
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("load class has error: {}, className: {}",
                                    ex.getMessage(), className);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("scanner package error:" + e);
        }
        if (printLossMemory && log.isInfoEnabled()) {
            log.info("path:{}, memory:{} kb", packageName, freeMemory - Runtime.getRuntime().freeMemory()/1024);
        }
        return set;
    }


    private void invokeDo(ResourceFileDo fileDo, String filter, Resource resource) throws IOException {

        if (fileDo == null)
            return;

        if (filter(filter, resource))
            return;

        fileDo.doHandler(resource.getFile());
    }



    boolean filter(String filter, Resource resource){

        if (filter  == null)
            return false;

        String name;
        try {

            name = resource.getFile().getName();
            return filterEntryParser.pass(name, filter);
        } catch (IOException e) {
            throw new RuntimeException("open file error", e);
        }
    }



    public static class FilterEntryParser{

        /* 元素分隔符 */
        final String ELEMENT_SPILT = ",";

        final String FILE_SPILT = ".";

        /* 左边框 */
        final String LEFT_FRAME = "[";

        /* 右边框 */
        final String RIGHT_FRAME = "]";

        /* 反义 */
        final String CONTRARY = "!";


        public boolean pass(String entry, String filterInstructions){

            int index;
            if( (index = entry.lastIndexOf(FILE_SPILT)) == -1)
                throw new RuntimeException("entry does not meet the parsing requirements, " + entry);

            /* 需要匹配的条目 */
            String matching = entry.substring(index);

            /* 此条目是否具有反义含义 */
            boolean contrary = matching.startsWith(CONTRARY);

            return contrary != analysisFilter(filterInstructions).contains(matching);
        }

        /***
         * 解析过滤条目
         * [xxx, xxx, xxx] 目前只支持该格式
         * @param filter 过滤条目
         * @return 将过滤的总条目解析成具体的 字符串集合
         */
        List<String> analysisFilter(String filter){

            if (filter == null)
                return new ArrayList<>(0);
            ArrayList<String> list = new ArrayList<>();

            if (filter.startsWith(LEFT_FRAME))
                filter = filter.substring(filter.indexOf(LEFT_FRAME));
            if (filter.endsWith(RIGHT_FRAME))
                filter = filter.substring(0, filter.indexOf(RIGHT_FRAME));
            String[] split = filter.split(ELEMENT_SPILT);
            for (String s : split)
                    list.add(s.trim());
            return list;
        }
    }

    private String resolveBasePackage(String basePackage) {
        String placeholders = null;
        if (applicationContext != null){
            // 解析占位符
            placeholders = this.applicationContext.getEnvironment().resolveRequiredPlaceholders(basePackage);
        }else {
            ConfigurableEnvironment environment = SpringEnvironmentHodler.getEnvironment();
            if (environment != null){
                placeholders = environment.resolveRequiredPlaceholders(basePackage);
            }
        }
        // 将类名转换为资源路径
        return ClassUtils.convertClassNameToResourcePath(placeholders == null ? basePackage : placeholders);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
