package com.black.core.permission;

import com.black.core.spring.ChiefApplicationRunner;
import com.black.pattern.Premise;

public class EnabledPermissionPremise implements Premise {
    @Override
    public boolean premise() {
        return ChiefApplicationRunner.isPertain(EnabledRUPComponent.class);
    }
}
