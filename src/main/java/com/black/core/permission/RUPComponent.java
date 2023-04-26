package com.black.core.permission;

import com.black.premission.GlobalRUPConfiguration;
import com.black.premission.GlobalRUPConfigurationHolder;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;

@LoadSort(423)
@LazyLoading(EnabledRUPComponent.class)
public class RUPComponent implements OpenComponent {

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        GlobalRUPConfiguration configuration = GlobalRUPConfigurationHolder.getConfiguration();
        configuration.load();
    }

}
