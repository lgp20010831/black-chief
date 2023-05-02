package com.black.core.http.code;

import com.black.core.chain.*;
import com.black.core.http.annotation.EnableHttpActuator;
import com.black.core.http.annotation.OpenHttp;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.tools.BeanUtil;
import com.black.holder.SpringHodler;
import com.black.utils.NameUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Log4j2
@LoadSort(154)  @ChainClient
@LazyLoading(EnableHttpActuator.class)
public class HttpActuatorImpl implements OpenComponent, CollectedCilent,
        ChainPremise{

    private final Collection<Object>  proxyClients = new HashSet<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        List<String> mapperNames = new ArrayList<>();
        for (Object proxyClient : proxyClients) {
            Class<Object> primordialClass = BeanUtil.getPrimordialClass(proxyClient);
            HttpFactory.cache.put(primordialClass, proxyClient);
            SpringHodler.getListableBeanFactory().registerSingleton(NameUtil.getName(proxyClient), proxyClient);
            mapperNames.add(primordialClass.getSimpleName());
        }

        if (!mapperNames.isEmpty()){
            if (log.isInfoEnabled()) {
                log.info("http mappers: {}", mapperNames);
            }
        }
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("http", false, new HttpProxyLayer(), rty ->{
           return rty.isInterface() && AnnotationUtils.getAnnotation(rty, OpenHttp.class) != null;
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("http".equals(resultBody.getAlias())){
            proxyClients.addAll(resultBody.getCollectSource());
        }
    }

    @Override
    public boolean premise() {
        return ChiefApplicationRunner.isPertain(EnableHttpActuator.class);
    }
}
