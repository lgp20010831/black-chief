package com.black.test;

import com.black.core.log.Catalog;
import com.black.core.mvc.FileUtil;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

public class TestApiApplication {

    private List<TestListener> listeners = new ArrayList<>();

    private ThreadPoolExecutor pool;

    private VfsScanner scanner;

    public void registerListener(TestListener listener){
        if (listener != null){
            listeners.add(listener);
        }
    }

    public List<RecordObject> test(Configuration configuration, String packageName){
        if (scanner == null){
            scanner = VFS.findVfsScanner();
        }

        List<RecordObject> recordObjects = new ArrayList<>();
        Set<Class<?>> classSet = scanner.load(packageName);
        for (Class<?> type : classSet) {
            if (BeanUtil.isSolidClass(type)){
                recordObjects.addAll(test(configuration, type));
            }
        }
        return recordObjects;
    }

    public List<RecordObject> test(Configuration configuration, Class<?> controllerClass){
        return test(configuration, controllerClass, null);
    }

    public List<RecordObject> test(Configuration configuration, Class<?> controllerClass, String absolutePath){
        ApiInfoGetter infoGetter = configuration.getApiInfoGetter();
        Catalog log = configuration.getLog();
        String complateUrl = configuration.getComplateUrl();
        ClassWrapper<?> classWrapper = ClassWrapper.get(controllerClass);
        List<RecordObject> recordObjects = new ArrayList<>();
        List<MethodWrapper> wrappers = parse(classWrapper, configuration);
        log.debug("===============================================");
        log.debug("       测试控制器:  [" + classWrapper.getSimpleName() + "]  ");
        log.debug("===============================================");
        int count = 0;
        int successful = 0;
        int fail = 0;
        for (MethodWrapper mw : wrappers) {
            String[] requestUrls = infoGetter.getRequestUrls(mw, classWrapper);
            String[] requestMethods = infoGetter.getRequestMethods(mw, classWrapper);
            Map<String, String> headerMap = infoGetter.getHeaderMap(mw, classWrapper);
            Object example = infoGetter.getRequestExample(mw, classWrapper);
            log.debug("======>       测试接口方法: [" + mw.getName() + "]");
            for (String requestUrl : requestUrls) {
                requestUrl = StringUtils.removeIfEndWith(complateUrl, "/") + "/" + StringUtils.removeIfStartWith(requestUrl, "/");
                for (String requestMethod : requestMethods) {
                    for (TestListener listener : listeners) {
                        requestUrl = listener.postUrl(requestUrl, mw, classWrapper);
                    }
                    for (TestListener listener : listeners) {
                        listener.postHttpHeaders(headerMap, mw, classWrapper);
                    }

                    RecordObject object = new RecordObject(classWrapper, mw, requestUrl, requestMethod);
                    for (ApiInvoker invoker : configuration.getInvokers()) {
                        if (invoker.supportMethod(requestMethod)) {
                            for (TestListener listener : listeners) {
                                example = listener.postRequestParam(example, mw, classWrapper);
                            }
                            try {

                                String response = execute(requestUrl, headerMap, example, object, configuration, invoker);
                                for (TestListener listener : listeners) {
                                    try {
                                        response = listener.postResponse(response, mw, classWrapper);
                                    }catch (Throwable ex){}
                                }
                                log.info("Response ===> \n" + response);
                                successful++;
                                object.setResponseBody(response);
                            } catch (Throwable e) {
                                fail++;
                                log.error("invoke error ===> \n" + e.getMessage());
                                object.setErrorMsg(e.getMessage());
                            }
                            break;
                        }
                    }
                    count++;
                    log.debug(" --------------------end--------------------- ");
                    recordObjects.add(object);
                }
            }
        }
        log.debug("总共测试接口数量: [" + count + "], 成功: [" + successful + "], 失败: [" + fail + "]");
        if (absolutePath != null){
            flushFile(absolutePath, log);
        }
        log.flush();
        return recordObjects;
    }

    protected void flushFile(String path, Catalog catalog){
        try {

            InputStream in = catalog.getInputStream();
            File file = FileUtil.dropAndcreateFile(path);
            FileUtil.writerFile(file, catalog.stringStack());
            System.out.println("成功写入日志文件, 文件地址: " + path);
        }catch (Throwable e){
            System.out.println("无法将日志信息写入文件中");
            System.out.println(e.getMessage());
        }
    }


    private List<MethodWrapper> parse(@NonNull ClassWrapper<?> controllerClass, Configuration configuration){
        Collection<MethodWrapper> methodByAnnotation = controllerClass.getMethods();

        //找出所有带有 requestmapping 注解的方法
        List<MethodWrapper> methodWrappers = StreamUtils
                .filterList(methodByAnnotation, mba -> AnnotationUtils.getAnnotation(mba.getMethod(), RequestMapping.class) != null
                        && (!configuration.isAnnotationLimit() || AnnotationUtils.getAnnotation(mba.getMethod(), TestedNozzle.class) != null));
        methodWrappers.sort((o1, o2) -> {
            TestedNozzle t1 = o1.getAnnotation(TestedNozzle.class);
            TestedNozzle t2 = o2.getAnnotation(TestedNozzle.class);
            return (t2 != null && t1 != null) ? t1.value() - t2.value() : 0;
        });
        return methodWrappers;
    }

    private void initPool(int size){
        if(pool == null){
            pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(size);
        }
    }

    protected String execute(String requestUrl, Map<String, String> headerMap, Object param,
                             RecordObject object, Configuration configuration, ApiInvoker invoker) throws Throwable{
        if (configuration.isConcurrency()) {
            final int p = configuration.getParallelValue();
            CountDownLatch downLatch = new CountDownLatch(p);
            initPool(p);
            AtomicReference<String> reference = new AtomicReference<>();
            for (int i = 0; i < p; i++) {
                pool.execute(() -> {
                    try {
                        String response = invoker.execute(requestUrl, headerMap, param, object);
                        reference.set(response);
                        downLatch.countDown();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            downLatch.await();
            return reference.get();
        }else {
            return invoker.execute(requestUrl, headerMap, param, object);
        }
    }
}
