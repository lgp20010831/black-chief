package com.black.core.permission;

import com.black.pattern.Premise;
import com.black.core.spring.ChiefApplicationRunner;

public class EnabledPermissionPremise implements Premise {
    @Override
    public boolean premise() {
        Class<?> mainClass = ChiefApplicationRunner.getMainClass();
        return mainClass != null && mainClass.isAnnotationPresent(EnabledRUPComponent.class);
    }
}
