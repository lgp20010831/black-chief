package com.black.callback;

import com.black.aop.AopDemo;
import com.black.aop.AopLoadRegister;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author 李桂鹏
 * @create 2023-06-16 14:04
 */
@SuppressWarnings("all")
public class AopLoadListener implements SpringApplicationRunListener {

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        AopLoadRegister.getInstance().load(AopDemo.class);
    }
}
