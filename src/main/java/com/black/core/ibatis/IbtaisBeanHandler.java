package com.black.core.ibatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.util.ClassUtils;
import com.black.ibtais.IbatisBean;
import com.black.ibtais.IbtatisUtils;
import com.black.javassist.CtAnnotation;
import com.black.javassist.CtAnnotations;
import com.black.javassist.PartiallyCtClass;
import com.black.spring.ChiefSpringHodler;
import com.black.utils.NameUtil;
import com.black.utils.ReflectionUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * @author 李桂鹏
 * @create 2023-05-05 14:05
 */
@SuppressWarnings("all")
public class IbtaisBeanHandler {


    public void handle(Class<?> type){
        if (!IbatisBean.class.isAssignableFrom(type)){
            return;
        }

        if (IbatisBean.class.equals(type)){
            return;
        }

        DefaultListableBeanFactory beanFactory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        Class<?> mapperType;
        BaseMapper<?> mapper = IbtatisUtils.autoFindMapper(type);
        if (mapper == null){
            mapperType = createBaseMapper(type);
            registerBean(mapperType);
        }else {
            mapperType = mapper.getClass();
        }
        IService<?> iService = IbtatisUtils.autoFindService(type);
        if (iService == null){
            Class<?> service = createIService(type, mapperType);
            Object instance = ReflectionUtils.instance(service);
            beanFactory.registerSingleton(NameUtil.getName(service), instance);
        }
    }

    protected void registerBean(Class<?> type){
        DefaultListableBeanFactory beanFactory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(type).getBeanDefinition();
        beanFactory.registerBeanDefinition(NameUtil.getName(type), beanDefinition);
    }

    protected Class<?> createBaseMapper(Class<?> type){
        String mapperName = type.getSimpleName() + "Mapper";
        //获取启动路径
        String packageName = ClassUtils.getPackageName(ChiefApplicationRunner.getMainClass());
        PartiallyCtClass partiallyCtClass = PartiallyCtClass.face(mapperName, packageName);
        partiallyCtClass.setSuperClass(BaseMapper.class);
        partiallyCtClass.setInterfaceGenericity(BaseMapper.class, type);
        CtAnnotation ctMapper = new CtAnnotation(Mapper.class);
        CtAnnotation ctRepository = new CtAnnotation(Repository.class);
        partiallyCtClass.addClassAnnotations(CtAnnotations.group(ctMapper, ctRepository));
        Class<?> javaClass = partiallyCtClass.getJavaClass();
        return javaClass;
    }


    protected Class<?> createIService(Class<?> type, Class<?> mapperType){
        String serviceName = type.getSimpleName() + "Impl";
        //获取启动路径
        String packageName = ClassUtils.getPackageName(ChiefApplicationRunner.getMainClass());
        PartiallyCtClass partiallyCtClass = PartiallyCtClass.make(serviceName, packageName);
        partiallyCtClass.setSuperClass(ServiceImpl.class);
        partiallyCtClass.setSuperClassGenericity(ServiceImpl.class, mapperType, type);
        CtAnnotation ctService = new CtAnnotation(Service.class);
        partiallyCtClass.addClassAnnotations(CtAnnotations.group(ctService));
        return partiallyCtClass.getJavaClass();
    }

}
