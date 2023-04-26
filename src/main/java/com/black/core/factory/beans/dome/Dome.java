package com.black.core.factory.beans.dome;




import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.spring.util.ApplicationUtil;


public class Dome {


    public static void main(String[] args) throws Exception {

        BeanFactory factory = FactoryManager.initAndGetBeanFactory();
        ApplicationUtil.programRunMills(() ->{
            Action action = factory.getSingleBean(Action.class);
            System.out.println(action);
        });

        factory.clearAll();
    }


}
