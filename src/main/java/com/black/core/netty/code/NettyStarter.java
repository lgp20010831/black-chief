package com.black.core.netty.code;

import com.black.core.chain.*;
import com.black.core.json.ReflexUtils;
import com.black.core.netty.annotation.*;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.core.tools.BeanUtil;
import com.black.netty.*;
import com.black.netty.branch.*;
import com.black.utils.ReflexHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Log4j2
@LoadSort(66)
@ChainClient(NettyStarter.NettyPremise.class)
@LazyLoading(EnableNettyAsynAdapter.class)
public class NettyStarter implements OpenComponent, CollectedCilent, ApplicationDriver {

    private final Collection<Object> nettyUsers = new HashSet<>();
    private final Map<String, NettyUserWrapper> nettyUserWrappers = new HashMap<>();

    @Override
    public void whenApplicationStop(ChiefExpansivelyApplication application) {
        nettyUserWrappers.forEach((alias, wrapper) ->{
            wrapper.getNettySession().close();
            if (log.isInfoEnabled()) {
                log.info("close netty user -- {}", alias);
            }
        });
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        for (Object nettyUser : nettyUsers) {
            if (nettyUser != null){
                Class<Object> primordialClass = BeanUtil.getPrimordialClass(nettyUser);
                NettyServer nettyServer = AnnotationUtils.getAnnotation(primordialClass, NettyServer.class);
                if (nettyServer != null){
                    handlerServerUser(nettyUser, nettyServer);
                }
                NettyClient nettyClient = AnnotationUtils.getAnnotation(primordialClass, NettyClient.class);
                if (nettyClient != null){
                    handlerClientUser(nettyUser, nettyClient);
                }
            }
        }
    }

    protected void handlerServerUser(Object user, NettyServer nettyServer){
        Configuration configuration = new Configuration();
        String alias = nettyServer.value();
        com.black.core.util.AnnotationUtils.loadAttribute(nettyServer, configuration);
        handlerUser(user, configuration);
        NettyServerSessionFactory sessionFactory = new NettyServerSessionFactory(configuration);
        createUserWrapper(sessionFactory, user, alias);
    }

    protected void handlerClientUser(Object user, NettyClient nettyClient){
        Configuration configuration = new Configuration();
        String alias = nettyClient.value();
        com.black.core.util.AnnotationUtils.loadAttribute(nettyClient, configuration);
        handlerUser(user, configuration);
        NettySessionFactory sessionFactory = new NettySessionFactory(configuration);
        createUserWrapper(sessionFactory, user, alias);
    }

    protected void createUserWrapper(SessionFactory<NettySession> sessionFactory, Object user, String alias){
        NettySession nettySession = sessionFactory.openSession();
        NettyUserWrapper nettyUserWrapper = new NettyUserWrapper(alias, sessionFactory, nettySession, user);
        nettyUserWrappers.put(alias, nettyUserWrapper);
        processorUser(user, nettyUserWrapper);
    }

    protected void handlerUser(Object user, Configuration configuration){
        if (user instanceof CloseSettlement){
            configuration.setCloseSettlement((CloseSettlement) user);
        }
        if (user instanceof ConnectComplete){
            configuration.setConnectComplete((ConnectComplete) user);
        }
        if (user instanceof LossConnection){
            configuration.setLossConnection((LossConnection) user);
        }
        if (user instanceof ReadComplete){
            configuration.setReadComplete((ReadComplete) user);
        }
        if (user instanceof ReadMessage){
            configuration.setReadMessage((ReadMessage) user);
        }
        if (user instanceof StartComplete){
            configuration.setStartComplete((StartComplete) user);
        }
        if (user instanceof ThrowableCaught){
            configuration.setThrowableCaught((ThrowableCaught) user);
        }
    }

    protected void processorUser(Object user, NettyUserWrapper userWrapper){
        for (Field field : ReflexHandler.getAccessibleFields(user)) {
            NettyAttribute attribute = AnnotationUtils.getAnnotation(field, NettyAttribute.class);
            if (attribute != null){
                String name = attribute.name();
                if (StringUtils.hasText(name)){
                    Object obj = userWrapper.get(name);
                    ReflexUtils.setValue(field, user, obj);
                }else {
                    Class<?> value = attribute.value();
                    if (!Void.class.equals(value)){
                        Object obj = userWrapper.get(value);
                        ReflexUtils.setValue(field, user, obj);
                    }else {
                        Class<?> type = field.getType();
                        Object obj = userWrapper.get(type);
                        ReflexUtils.setValue(field, user, obj);
                    }
                }
            }
        }
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin("netty", rc -> AnnotationUtils.getAnnotation(rc, NettyUser.class) != null &&
                BeanUtil.isSolidClass(rc));
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if (resultBody.getAlias().equals("netty")){
            nettyUsers.addAll(resultBody.getCollectSource());
        }
    }

    public static class NettyPremise implements ChainPremise {

        @Override
        public boolean premise() {
            return ChiefApplicationRunner.isPertain(EnableNettyAsynAdapter.class);
        }
    }
}
