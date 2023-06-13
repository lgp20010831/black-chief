package com.black.core.fun_net;

import com.black.core.factory.beans.InitMethod;
import com.black.core.factory.beans.config_collect520.Collect;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.driver.ApplicationDriver;
import com.black.fun_net.FunNetRegister;
import com.black.fun_net.Net;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Set;

/**
 * @author 李桂鹏
 * @create 2023-06-08 11:19
 */
@SuppressWarnings("all") @Log4j2
public class NetComponent implements OpenComponent, ApplicationDriver {

    @Collect(type = Net.class) Set<Class<Net>> nets;

    @Override
    public void whenApplicationStart(ChiefExpansivelyApplication application) {
        System.out.println(nets);
        FunNetRegister register = FunNetRegister.getInstance();
        for (Class<Net> net : nets) {
            Object proxy = register.loadClass(net);
            log.info("Loading functional interface completed: {}", proxy);
        }
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) throws Throwable {

    }
}
