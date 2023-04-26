package com.black.core.asyn;

import com.black.core.chain.ChainClient;
import com.black.core.chain.CollectedCilent;
import com.black.core.chain.ConditionResultBody;
import com.black.core.chain.QueryConditionRegister;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.IgnorePrint;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.tools.BeanUtil;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;
import java.util.HashSet;

@IgnorePrint
@LoadSort(416)
@ChainClient
public class AsynGlobalManagementCenter implements CollectedCilent, OpenComponent {

    private final Collection<Object> managers = new HashSet<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        AsynConfiguration configuration = AsynConfigurationManager.getConfiguration();
        for (Object manager : managers) {
            ManageConfiguration manageConfiguration = (ManageConfiguration) manager;
            manageConfiguration.postConfiguration(configuration);
        }
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("asyn", srt -> ManageConfiguration.class.isAssignableFrom(srt) &&
                BeanUtil.isSolidClass(srt) && AnnotationUtils.getAnnotation(srt, AsynAdministrators.class) != null);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("asyn".equals(resultBody.getAlias())){
            managers.addAll(resultBody.getCollectSource());
        }
    }
}
