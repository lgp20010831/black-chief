package com.black.core.mybatis.intercept.code;

import com.black.core.mybatis.MybatisLayerObject;
import com.black.core.mybatis.SqlLogIntercept;
import com.black.core.mybatis.intercept.annotation.DynamicallyIbtaisIntercept;

@DynamicallyIbtaisIntercept({"prepare", "result"})
public class DynamicallySqlLogIntercept extends SqlLogIntercept {

    @Override
    public Object doIntercept(MybatisLayerObject layer) {
        return super.doIntercept(layer);
    }
}
