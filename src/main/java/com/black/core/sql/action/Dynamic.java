package com.black.core.sql.action;

import com.black.core.util.StringUtils;

public interface Dynamic {

    default String name(){
        return StringUtils.titleLower(getClass().getSimpleName());
    }


}
