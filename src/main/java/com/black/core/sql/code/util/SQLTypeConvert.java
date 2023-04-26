package com.black.core.sql.code.util;

import com.black.core.convert.ConversionWay;
import com.black.core.convert.TypeContributor;

@TypeContributor
public class SQLTypeConvert {

    @ConversionWay
    boolean cb(Boolean b){
        return b != null && b;
    }

}
