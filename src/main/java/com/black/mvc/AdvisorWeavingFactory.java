package com.black.mvc;

import com.black.holder.SpringHodler;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Map;

//advice = intercept
//advisor = advice + pointcut
public class AdvisorWeavingFactory {

    private static final IoLog log = LogFactory.getArrayLog();

    public static Object proxy(Object bean){
        return proxy(bean, true);
    }

    public static Object proxy(Object bean, boolean setAdvisor){

        DefaultListableBeanFactory beanFactory = SpringHodler.getNonNullListableBeanFactory();
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setOptimize(true);
        if (setAdvisor){
            Map<String, Advisor> advisorMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, Advisor.class);
            proxyFactory.addAdvisors(advisorMap.values());
        }
        log.trace("[AdvisorWeavingFactory] create spring proxy ===> {}", bean);
        return proxyFactory.getProxy();
    }

    //advisor  advice  pointcut


    /*
        @Aspect    //把 myAop ==> advisor
        class myAop{

            @Pointcut(@RequestMapping)  //===> pointCut
            void log(){}

            @Around         // ==> advice
            Object do0(){

            }
        }

     */

}
