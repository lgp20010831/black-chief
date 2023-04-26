package com.black.callback;

import com.black.core.spring.ChiefApplicationHolder;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

public class ChiefApplicationLoadChecker implements SpringApplicationRunListener {


    @Override
    public void started(ConfigurableApplicationContext context) {
        if (!ChiefApplicationRunner.isOpen()) {
            return;
        }

        ChiefExpansivelyApplication expansivelyApplication = ChiefApplicationHolder.getExpansivelyApplication();
        if (expansivelyApplication == null){
            return;
        }

        if (!expansivelyApplication.isLoad()){
            System.out.println("Correct the execution status of the application");
            expansivelyApplication.load();
        }
    }
}
