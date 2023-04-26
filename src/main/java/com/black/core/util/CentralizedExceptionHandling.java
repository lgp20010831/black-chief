package com.black.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("all")
public class CentralizedExceptionHandling {

    static Logger logger = LoggerFactory.getLogger(CentralizedExceptionHandling.class);


    public static void handlerException(Throwable e)    {handlerException(e,null);}

    public static void handlerException(Throwable e,String message){

        try {

            //do something
        }finally {

            //打印日志
            logger.error((message == null ? "":"message:"+message+";")+"执行中发生异常:"+ ExceptionUtil.getStackTraceInfo(e));
        }
    }
}
