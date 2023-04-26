package com.black.core.aop.servlet.flow;

import com.black.JsonBean;
import com.black.core.json.ReflexUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.function.Supplier;

@Getter @Setter @SuppressWarnings("all")
public class FlowConfiguration extends JsonBean {


    Set<FlowTimeUnit> units;

    //限流表达式, min: 90 -- 表示每分钟最大访问次数为 90 次
    //           second: xxxx, hour:xxx, 前面的单位必需是 units 中存在的
    Set<String> limitExpression;

    //针对用户
    Set<String> userLimitExpression;

    //打印访问信息
    boolean print;

    Class<? extends Supplier<Object>> limitResponse;

    Supplier<Object> responser;

    public Supplier<Object> getLimitResponser(){
        if (limitResponse == null || limitResponse.equals(Supplier.class)){
            return new Supplier<Object>() {
                @Override
                public Object get() {
                    return null;
                }
            };
        }

        if (responser == null){
            responser = ReflexUtils.instance(limitResponse);
        }
        return responser;
    }
}
