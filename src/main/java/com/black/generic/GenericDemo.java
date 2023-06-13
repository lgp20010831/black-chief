package com.black.generic;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.InitMethod;
import com.black.core.factory.beans.config_collect520.Collect;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.query.ClassWrapper;
import com.black.core.spring.OpenComponent;
import com.black.fun_net.Net;
import com.black.javassist.PartiallyCtClass;
import com.black.utils.ReflectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author 李桂鹏
 * @create 2023-06-09 10:58
 */
@SuppressWarnings("all")
public class GenericDemo {


    public static class Test{

        @Collect
        Map<String, Class<OpenComponent>> list;

        @InitMethod
        void init(@Collect Set<Class<Net>> set){
            System.out.println(set);
        }
    }

    public static void main(String[] args) throws NoSuchFieldException {
//        BeanFactory factory = FactoryManager.initAndGetBeanFactory();
//        Test test = factory.getSingleBean(Test.class);
        Field field = Test.class.getDeclaredField("list");
        System.out.println(field.toGenericString());
        GenericInfo info = GenericUtils.getGenericByField(field);
        System.out.println(info);
        System.out.println(1);

        PartiallyCtClass partiallyCtClass = PartiallyCtClass.make("Demo");

        partiallyCtClass.createField("name", Map.class, null,
                GenericInfo.group(Generic.of(String.class), Generic.of(List.class, Generic.of(Net.class))));
        Class<?> javaClass = partiallyCtClass.getJavaClass();
        ClassWrapper<?> classWrapper = ClassWrapper.get(javaClass);
        System.out.println(classWrapper);
    }
}
