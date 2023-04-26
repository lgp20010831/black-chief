package com.black.ibtais;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.black.holder.SpringHodler;
import com.black.core.json.JsonUtils;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.tools.BeanUtil;
import com.black.utils.ReflectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Map;

@SuppressWarnings("all")
public class IbtatisUtils {

    private static AliasColumnConvertHandler convertHandler = new HumpColumnConvertHandler();

    public static void setConvertHandler(AliasColumnConvertHandler convertHandler) {
        IbtatisUtils.convertHandler = convertHandler;
    }

    public static AliasColumnConvertHandler getConvertHandler() {
        return convertHandler;
    }

    public static <T> BaseMapper<T> autoFindMapper(Class<T> type){
        DefaultListableBeanFactory beanFactory = SpringHodler.getNonNullListableBeanFactory();
        IService<T> tiService = autoFindService(type);
        if (tiService != null){
            return tiService.getBaseMapper();
        }

        Map<String, BaseMapper> baseMapperMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, BaseMapper.class);
        for (BaseMapper baseMapper : baseMapperMap.values()) {
            Class<BaseMapper> primordialClass = BeanUtil.getPrimordialClass(baseMapper);
            Class<?>[] genericVals = ReflectionUtils.genericVal(primordialClass, BaseMapper.class);
            if (genericVals.length == 1){
                if (genericVals[0].equals(type)){
                    return baseMapper;
                }
            }
        }
        return null;
    }

    public static <T> IService<T> autoFindService(Class<T> type){
        DefaultListableBeanFactory beanFactory = SpringHodler.getNonNullListableBeanFactory();
        Map<String, IService> baseMapperMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, IService.class);
        for (IService service : baseMapperMap.values()) {
            if (type.equals(service.getEntityClass())){
                return service;
            }
        }
        return null;
    }

    public static <T> ActionWrapperResolver<T> createWrapperResolver(String blendString){
        return new ActionWrapperResolver<>(new BusinessTransformation() {
            @Override
            public AliasColumnConvertHandler getConvertHandler() {
                return convertHandler;
            }

            @Override
            public String getBlendString() {
                return blendString;
            }
        });
    }

    public static UpdateWrapper<T> wiredUpdateWrapper(Class<T> type, Map<String, Object> map){
        return wiredUpdateWrapper(new UpdateWrapper<>(), type, map);
    }

    public static <T> UpdateWrapper<T> wiredUpdateWrapper(UpdateWrapper<T> wrapper, Class<T> type, Map<String, Object> map){
        ActionWrapperResolver<T> resolver = createWrapperResolver(null);
        return resolver.wiredUpdateWrapper(wrapper, type, map);
    }

    public static <T> AbstractWrapper<T, String, ?> wriedWrapper(AbstractWrapper<T, String, ?> wrapper,
                                                                 T bean){
        return wriedWrapper(wrapper, bean, null);
    }

    public static <T> AbstractWrapper<T, String, ?> wriedWrapper(AbstractWrapper<T, String, ?> wrapper,
                                                                 T bean, String blendString){
        Class<T> primordialClass = BeanUtil.getPrimordialClass(bean);
        JSONObject jsonObject = JsonUtils.letJson(bean);
        return wriedWrapper(wrapper, primordialClass, jsonObject, blendString);
    }

    public static <T> AbstractWrapper<T, String, ?> wriedWrapper(AbstractWrapper<T, String, ?> wrapper,
                                                                 Class<T> type,
                                                                 Map<String, Object> body){
        return wriedWrapper(wrapper, type, body, null);
    }

    public static <T> AbstractWrapper<T, String, ?> wriedWrapper(AbstractWrapper<T, String, ?> wrapper,
                                                             Class<T> type,
                                                             Map<String, Object> body,
                                                             String blendString){
        ActionWrapperResolver<T> resolver = createWrapperResolver(blendString);
        return resolver.wriedWrapper(wrapper, type, body);
    }


}
