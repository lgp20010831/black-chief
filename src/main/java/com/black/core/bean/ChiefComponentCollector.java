package com.black.core.bean;

import com.black.bin.InstanceType;
import com.black.core.annotation.ChiefComponent;
import com.black.core.chain.*;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.utils.NameUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Collection;

@Log4j2
@ChainClient
public class ChiefComponentCollector implements CollectedCilent {

    private final DefaultListableBeanFactory beanFactory;

    public ChiefComponentCollector(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin("bean", ty -> {
            return BeanUtil.isSolidClass(ty) && ty.isAnnotationPresent(ChiefComponent.class);
        });
        entry.setInstanceType(InstanceType.BEAN_FACTORY);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        Collection<Object> collectSource = resultBody.getCollectSource();
        for (Object bean : collectSource) {
            Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
            ChiefComponent annotation = primordialClass.getAnnotation(ChiefComponent.class);
            if (annotation != null) {
                if (annotation.registerSpring() && beanFactory != null){
                    String value = annotation.value();
                    String beanName = StringUtils.hasText(value) ? value : NameUtil.getName(bean);
                    try {
                        beanFactory.registerSingleton(beanName, bean);
                    }catch (BeansException e){
                        log.warn("can not register chief component to spring, of:{}", e.getMessage());
                    }

                }
            }
        }
        log.info("finish create chief component size:[{}]", collectSource.size());
    }
}
