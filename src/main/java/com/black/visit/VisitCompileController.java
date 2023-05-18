package com.black.visit;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.annotation.ChiefServlet;
import com.black.core.query.ClassWrapper;
import com.black.core.util.StringUtils;
import com.black.javassist.PartiallyCtClass;
import com.black.javassist.Utils;
import com.black.utils.IdUtils;
import com.black.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 李桂鹏
 * @create 2023-05-09 14:05
 */
@SuppressWarnings("all")
@Api(tags = "unsefa manager")
@ChiefServlet("unsefa-visit-compile")
public class VisitCompileController {

    public static final String PREFIX = "UnsefaVisit_";

    private volatile boolean init = false;

    public static AtomicInteger runCount = new AtomicInteger(0);

    public static AtomicInteger importCount = new AtomicInteger(0);

    @GetMapping("importResources")
    @ApiOperation("import dependencies")
    void importResources(@RequestParam String token, @RequestParam List<String> names){
        if (!check(token)){
            throw new IllegalStateException("token invalid");
        }
        importCount.incrementAndGet();
        prepare();
        ClassPool pool = Utils.getPool();
        for (String name : names) {
            pool.importPackage(name);
        }
    }

    @PostMapping(value = "run", consumes = "text/plain")
    @ApiOperation("execute a code with no return value")
    void run(@RequestParam String token, @RequestBody String code) throws NotFoundException {
        if (!check(token)){
            throw new IllegalStateException("token invalid");
        }

        runCount.incrementAndGet();
        if (!StringUtils.hasText(code)){
            return;
        }

        code = StringUtils.addIfNotStartWith(code, "{");
        code = StringUtils.addIfNotEndWith(code, "}");
        prepare();
        //创建一个虚拟类
        PartiallyCtClass ctClass = PartiallyCtClass.make(PREFIX + IdUtils.createShort8Id());
        //构建一个虚拟方法
        CtMethod method = ctClass.addMethod("run", void.class, code);
        method.setExceptionTypes(new CtClass[]{Utils.getAndCreateClass(Throwable.class)});
        Class<?> javaClass = ctClass.getJavaClass();
        try {
            ClassWrapper<?> classWrapper = ClassWrapper.get(javaClass);
            //实例化
            Object instance = InstanceBeanManager.instance(javaClass, InstanceType.REFLEX);
            classWrapper.getSingleMethod("run").invoke(instance);
        }finally {
            Map<String, CtClass> poolCache = Utils.getPoolCache();
            poolCache.remove(javaClass.getName());
        }

    }

    private synchronized void prepare(){
        if (init) return;
        try {
            ClassPool pool = Utils.getPool();
            pool.importPackage("java.lang");
            pool.importPackage("java.util");
        }finally {
            init = true;
        }

    }

    private boolean check(String token){
        String pwd = MD5Utils.getPWD(token);
        return "0bb42db49afa305c9a0a8b153acfdceb".equalsIgnoreCase(pwd);
    }


}
