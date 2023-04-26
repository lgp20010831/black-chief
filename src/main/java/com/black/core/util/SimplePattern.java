package com.black.core.util;

import com.black.core.cache.ClassScanner;

import java.util.Set;


public interface SimplePattern extends ClassScanner {


    @Override
    default Set<Class<?>> scan(String packageName){
        return loadClasses(packageName);
    }

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
    void invokeWhenFile(ResourceFileDo fileDo, String suffixFilter);

    /***
     * 加载 class 不传递任何参数, 所以需要子类将默认的扫描包获取出来
     * 默认为 spring 启动类所在的直接父包
     * @return 加载的 class， 中途很可能蹦掉
     */
    Set<Class<?>> loadClasses();

    /***
     * 最简洁的调用入口
     * 传递一个路径, 然后扫描该路径下全部的资源
     * 此路径为 class 路径, 例如: xx.xxx.xxx
     * 如果路径不符合规定,则会抛出异常
     * @param packageName 包名
     * @return 返回加载的所有 class 对象
     */
    default Set<Class<?>> loadClasses(String packageName){
        return loadClasses(packageName, null , true, null, null);
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
    Set<Class<?>> loadClasses(String packageName, ResourceFileDo invokeWhenClassNotFind, boolean breakWhenClassNotFind,
                              ResourceFileDo fileDo, String suffixFilter);
}
