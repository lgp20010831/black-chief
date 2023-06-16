package com.black.aop;

import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;

/**
 * @author 李桂鹏
 * @create 2023-06-16 13:41
 */
@SuppressWarnings("all")
public class AopAdapter {


    static {
        AdvisorAdapterRegistry registry = GlobalAdvisorAdapterRegistry.getInstance();

    }


}
