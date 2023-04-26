package com.black.core.mybatis;

import com.black.holder.SpringHodler;
import com.black.core.chain.*;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.tools.BeanUtil;


@ChainClient(MybatisPremise.class)
@LazyLoading(EnableIbatisInterceptsDispatcher.class)
public class MybatisInterceptsTaskChain implements OpenComponent, CollectedCilent {

    private MybatisInterceptsConfiguartion mybatisInterceptsConfiguartion;

    private MybatisInterceptsDispatcher mybatisInterceptsDispatcher;

    private IbtaisInterceptsPointHandler ibtaisInterceptsPointHandler = new IbtaisInterceptsPointHandler();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        InstanceFactory instanceFactory = expansivelyApplication.instanceFactory();
        loadConfig(instanceFactory);
        getInterceptsDispatcher(expansivelyApplication);
        if (mybatisInterceptsDispatcher != null){
            mybatisInterceptsDispatcher.setMybatisInterceptsConfiguartion(mybatisInterceptsConfiguartion);

        }
        if (ibtaisInterceptsPointHandler != null && mybatisInterceptsDispatcher != null){
            ibtaisInterceptsPointHandler.setMybatisInterceptsConfiguartion(mybatisInterceptsConfiguartion);
            ibtaisInterceptsPointHandler.setMybatisInterceptsDispatcher(mybatisInterceptsDispatcher);
            ibtaisInterceptsPointHandler.instanceLayers(SpringHodler.getListableBeanFactory(), instanceFactory);
            mybatisInterceptsDispatcher.add(ibtaisInterceptsPointHandler.getMybatisLayers());
        }
    }


    protected void getInterceptsDispatcher(ChiefExpansivelyApplication chiefExpansivelyApplication){
        if (mybatisInterceptsDispatcher == null){
            mybatisInterceptsDispatcher = createDispatcher();
        }
    }

    protected MybatisInterceptsDispatcher createDispatcher(){
        return new MybatisInterceptsDispatcher();
    }

    protected void loadConfig(InstanceFactory instanceFactory){
        mybatisInterceptsConfiguartion = instanceFactory.getInstance(MybatisInterceptsConfiguartion.class);
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin("int", uty -> {
            return BeanUtil.isSolidClass(uty) && IbatisIntercept.class.isAssignableFrom(uty) && uty.isAnnotationPresent(MybatisIntercept.class);
        });
        entry.instance(false);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if (resultBody.getAlias().equals("int")) {
            for (Object obj : resultBody.getCollectSource()) {
                Class<? extends IbatisIntercept> aClass = (Class<? extends IbatisIntercept>) obj;
                MybatisIntercept mybatisIntercept = aClass.getAnnotation(MybatisIntercept.class);
                ibtaisInterceptsPointHandler.registerEarlyIntercept(aClass, mybatisIntercept.value());
            }
        }
    }


}
