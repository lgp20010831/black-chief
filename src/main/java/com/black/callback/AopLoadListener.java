package com.black.callback;

import com.black.aop.AopIntercept;
import com.black.aop.AopLoadRegister;
import com.black.core.annotation.Sort;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author 李桂鹏
 * @create 2023-06-16 14:04
 */
@SuppressWarnings("all") @Sort(800)
public class AopLoadListener implements SpringApplicationRunListener {

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        ApplicationStartingTaskManager.addTask(clazz -> {
            if (clazz.isAnnotationPresent(AopIntercept.class)){
                AopLoadRegister register = AopLoadRegister.getInstance();
                register.load(clazz);
            }
        });
        //AopLoadRegister.getInstance().load(AopDemo.class);
    }
}
