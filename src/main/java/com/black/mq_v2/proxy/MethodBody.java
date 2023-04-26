package com.black.mq_v2.proxy;

import com.black.core.chain.GroupKeys;
import com.black.core.query.MethodWrapper;
import com.black.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class MethodBody {

    private final MethodWrapper methodWrapper;

    private final Set<String> supportPatterns = new HashSet<>();

    private Object invokeBean;

    private final GroupKeys groupKeys;

    public MethodBody(MethodWrapper methodWrapper, Object invokeBean) {
        this.methodWrapper = methodWrapper;
        this.invokeBean = invokeBean;
        groupKeys = new GroupKeys(methodWrapper, invokeBean);
    }
    public void addPatterns(String... patterns){
        for (String pattern : patterns) {
            if (StringUtils.hasText(pattern)){
                supportPatterns.add(pattern);
            }
        }
    }


    @Override
    public boolean equals(Object obj) {
        return groupKeys.equals(obj);
    }

    @Override
    public int hashCode() {
        return groupKeys.hashCode();
    }

    @Override
    public String toString() {
        return "< method: " + methodWrapper.getName() + ", invokeBean: " + invokeBean + ">";
    }
}
