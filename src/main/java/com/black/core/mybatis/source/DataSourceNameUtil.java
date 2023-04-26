package com.black.core.mybatis.source;


import com.black.core.util.StringUtils;

public class DataSourceNameUtil {

    public static final String SUFFIX = "-datasource";

    public static String getDataSourceAlias(String sourceAlias){
        if (sourceAlias.endsWith(SUFFIX)){
            return sourceAlias;
        }
        return StringUtils.linkStr(sourceAlias, SUFFIX);
    }
}
