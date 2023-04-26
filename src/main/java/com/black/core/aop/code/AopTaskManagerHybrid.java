package com.black.core.aop.code;

//aop 管理节点
public interface AopTaskManagerHybrid {

    /** 提供一个匹配的东西 */
    AopMatchTargetClazzAndMethodMutesHandler obtainMatcher();

    /** 如果成功被 GlobalAopMamatchingDispatcher收集到, 则会调用此回调 */
    default void ifCollectCallBack(GlobalAopMamatchingDispatcher dispatcher){}

    /** 如果 class 和 方法匹配成功后由{@link GlobalAopMamatchingDispatcher} 进行回调此方法 */
    default void ifMatchCallBack(PitchClassWithMethodsWrapper wrapper){}

    AopTaskIntercepet obtainAopTaskIntercept();
}
