package com.black.core.aop.servlet.plus.handler;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.black.core.aop.servlet.plus.*;
import com.black.core.aop.servlet.plus.config.QueryWrapperConfiguration;
import com.black.core.builder.Col;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Map;

public class VirtualDeletionHandler extends AbstractWrapperHandler {

    public VirtualDeletionHandler(MappingPolicyHandler policyHandler) {
        super(policyHandler);
    }

    @Override
    public boolean support(PlusMethodWrapper methodWrapper, EntryWrapper entryWrapper) {
        return methodWrapper.getAnnotationMap().containsKey(WriedDeletionWrapper.class);
    }

    @Override
    public Wrapper<?> handler(PlusMethodWrapper methodWrapper,
                              EntryWrapper entryWrapper,
                              QueryWrapperConfiguration configuration) {
        Parameter paramter = methodWrapper.getWrapperParamter();
        UpdateWrapper<?> updateWrapper = new UpdateWrapper<>();
        WriedDeletionWrapper deletionWrapper = AnnotationUtils.getAnnotation(paramter, WriedDeletionWrapper.class);
        //将查询填充好
        handlerObject(entryWrapper.getArg(), updateWrapper, entryWrapper, methodWrapper, configuration, null);
        Map<String, String> map = MethodEntryExecutor.handlerFillSet(new HashSet<>(Col.as(deletionWrapper.autoInjection())));
        if (map.isEmpty()){
            throw new RuntimeException("虚拟删除需要指定删除的标志字段");
        }
        Map<String, Object> processor = MethodEntryExecutor.processor(map, entryWrapper, entryWrapper.getTotalSource());
        MappingPolicy policy = entryWrapper.getPolicy();
        processor.forEach((key, val) ->{
            updateWrapper.set(policyHandler.handlerByPolicy(key, policy), val);
        });
        return updateWrapper;
    }


}
