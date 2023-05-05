package com.black.core.ibatis;

import com.black.ibtais.IbatisBean;

/**
 * @author 李桂鹏
 * @create 2023-05-05 14:00
 */
@SuppressWarnings("all")
public class IbatisBeanEnhancedComponents {

    private static final IbtaisBeanHandler IBTAIS_BEAN_HANDLER = new IbtaisBeanHandler();

    public static void handleIbatisBean(Class<?> target){
        if (!EnvironmentalJudgment.isMybatisEnv()){
            return;
        }
        IBTAIS_BEAN_HANDLER.handle(target);
    }

}
