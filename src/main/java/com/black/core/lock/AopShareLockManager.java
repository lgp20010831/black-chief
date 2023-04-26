package com.black.core.lock;

import com.black.lock.ShareLock;
import com.black.lock.ShareLockLayer;
import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.aop.code.HijackObject;
import com.black.core.spring.factory.HijackAgentObject;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AopHybrid
@HybridSort(10)
public class AopShareLockManager implements AopTaskManagerHybrid, AopTaskIntercepet {

    private ShareLockLayer lockLayer;

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        ShareLockLayer lockLayer = getLockLayer();
        return lockLayer.proxy(HijackAgentObject.of(hijack));
    }

    public ShareLockLayer getLockLayer() {
        if (lockLayer == null){
            lockLayer = new ShareLockLayer();
        }
        return lockLayer;
    }

    /**
     * 提供一个匹配的东西
     */
    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent directAgent = AopMethodDirectAgent.getInstance();
        directAgent.register(this, ((targetClazz, method) -> {
            return method.isAnnotationPresent(ShareLock.class) || targetClazz.isAnnotationPresent(ShareLock.class);
        }));
        return directAgent.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        return this;
    }
}
