package com.black.ibtais;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.black.core.tools.BeanUtil;
import com.black.utils.ReflexHandler;

@SuppressWarnings("all")
public abstract class ObjectIbatisFullController<T> extends IbatisFullParentServlet<T>{

    private Class<T> entityClass = null;

    private BaseMapper<T> baseMapper;

    private IService<T> service;

    @Override
    protected Class<T> getEntityType() {
        if (entityClass != null){
            return entityClass;
        }
        Class<ObjectIbatisFullController<T>> primordialClass = BeanUtil.getPrimordialClass(this);
        Class<T> result = null;
        for (;;){
            Class<?>[] genericVal = ReflexHandler.superGenericVal(primordialClass);
            if (genericVal.length != 1){
                Class<?> superclass = primordialClass.getSuperclass();
                if (superclass != null && ObjectIbatisFullController.class.isAssignableFrom(superclass)){
                    primordialClass = (Class<ObjectIbatisFullController<T>>)superclass;
                    continue;
                }else {
                    throw new IllegalStateException("error for extends ObjectIbatisFullController:" + primordialClass);
                }
            }
            result = (Class<T>) genericVal[0];
            break;
        }
        entityClass = result;
        return result;
    }

    @Override
    protected BaseMapper<T> getMapper() {
        if (baseMapper == null){
            baseMapper = IbtatisUtils.autoFindMapper(getEntityType());
        }
        return baseMapper;
    }

    @Override
    protected <M extends BaseMapper<T>> ServiceImpl<M, T> getImpl() {
        if (service == null) {
            service = IbtatisUtils.autoFindService(getEntityType());
        }
        return (ServiceImpl<M, T>) service;
    }
}
