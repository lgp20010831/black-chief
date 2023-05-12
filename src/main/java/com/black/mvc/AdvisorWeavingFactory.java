package com.black.mvc;

import com.black.holder.SpringHodler;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.utils.ServiceUtils;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
            proxyFactory.addAdvisors(sortAdvisor(advisorMap.values()));
        }
        log.trace("[AdvisorWeavingFactory] create spring proxy ===> {}", bean);
        return proxyFactory.getProxy();
    }

    private static List<Advisor> sortAdvisor(Collection<Advisor> advisors){
        return ServiceUtils.sort(new ArrayList<>(advisors), advisor -> {
            if (advisor instanceof Ordered){
                return ((Ordered) advisor).getOrder();
            }
            Class<? extends Advisor> advisorClass = advisor.getClass();
            Order annotation = advisorClass.getAnnotation(Order.class);
            return annotation == null ? Ordered.LOWEST_PRECEDENCE : annotation.value();
        }, true);
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
