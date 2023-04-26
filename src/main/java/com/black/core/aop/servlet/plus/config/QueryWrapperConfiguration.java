package com.black.core.aop.servlet.plus.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
public class QueryWrapperConfiguration implements WrapperConfiuration {

    String pointArgName;
    String[] orConditionFields;
    String[] likeConditionFields;
    String[] conditionMap;
    String[] orderByAsc;
    String[] orderByDesc;
    String[] qualificationAsCondition;
    String[] invalidCondition;
    Set<String> annotationAualified;
    String ifArrayOperator;
    String ifArrayPointFieldName;
    boolean ifBaseBeanInvokeWriedDefaultValue;
    //最终操作的是 map, 所有有key存在,但是value为空, 才会符合该处理条件
    boolean ignoreNullValue;
    String applySql;

    public Set<String> getAnnotationExclusions(Set<String> fieldNames) {
        if(annotationAualified == null){
            annotationAualified = new HashSet<>();

            //将有资格的数据保存
            if (qualificationAsCondition != null) {
                if (qualificationAsCondition.length == 1 && "*".equals(qualificationAsCondition[0])){
                    annotationAualified.addAll(fieldNames);
                }else {
                    annotationAualified.addAll(Arrays.asList(qualificationAsCondition));
                }
            }
            if (invalidCondition != null){
                for (String icon : invalidCondition) {
                    annotationAualified.remove(icon);
                }
            }
        }
        return annotationAualified;
    }
}
