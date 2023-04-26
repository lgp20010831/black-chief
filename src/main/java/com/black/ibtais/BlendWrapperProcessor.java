package com.black.ibtais;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.black.core.sql.code.util.SQLUtils;

import java.util.List;

public class BlendWrapperProcessor {


    public void processor(String operator, String columnName, Object val, boolean or, AbstractWrapper<?, String, ?> wrapper){

        if (or){
            wrapper.or();
        }
        if (val == null){
            wrapper.isNull(columnName);
            return;
        }

        switch (operator){
            case "eq":
                wrapper.eq(columnName, val);
                break;
            case "like":
            case "LIKE":
                wrapper.like(columnName, val);
                break;
            case ">":
                wrapper.gt(columnName, val);
                break;
            case "<":
                wrapper.lt(columnName, val);
                break;
            case "<>":
                wrapper.ne(columnName, val);
                break;
            case "<=":
                wrapper.le(columnName, val);
                break;
            case ">=":
                wrapper.ge(columnName, val);
                break;
            case "in":
            case "IN":
                List<Object> list = SQLUtils.wrapList(val);
                wrapper.in(columnName, list);
                break;
            default:
                throw new IllegalStateException("无法支持的操作符: " + operator);
        }
    }
}
