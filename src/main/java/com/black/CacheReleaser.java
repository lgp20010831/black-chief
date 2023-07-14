package com.black;

import com.black.asm.User;
import com.black.core.api.ApiService;
import com.black.core.cache.ClassSourceCache;
import com.black.core.query.ClassWrapper;
import com.black.holder.SpringHodler;
import com.black.javassist.Utils;
import com.black.pattern.PropertyReader;
import com.black.spring.ChiefSpringHodler;
import javassist.CtClass;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-07-12 13:32
 */
@SuppressWarnings("all")
public class CacheReleaser {

    public static void release(){
        clearClassCache();
        clearJavassist();
        clearApi();
        System.gc();
    }

    public static void clearClassCache(){
        ClassSourceCache.clear();
        ClassWrapper.clearCache();
    }

    public static void clearJavassist(){
        Map<String, CtClass> poolCache = Utils.getPoolCache();
        Iterator<String> iterator = poolCache.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            CtClass ctClass = poolCache.get(key);
            if (ctClass != null && ctClass.getName().startsWith(Utils.FICTITIOUS_PATH)){
                iterator.remove();
            }
        }
    }

    public static void clearApi(){
        DefaultListableBeanFactory factory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        if (factory != null){
            try {
                ApiService service = factory.getBean(ApiService.class);
                service.setCache(null);
            }catch (Throwable e){

            }
        }
    }

    public static void main(String[] args) {
        PropertyReader.visitProperties(new User(), property -> {
            property.set(3);
            System.out.println(property);
        });
    }
}
