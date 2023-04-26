package com.black.core.log;

import com.black.core.util.StringUtils;
import com.black.utils.ServiceUtils;



public class LogUtils {


    public static String getCurrentInfo(){
        return StringUtils.linkStr(ServiceUtils.v2Now(), " [", Thread.currentThread().getName(), "] ");
    }

}
