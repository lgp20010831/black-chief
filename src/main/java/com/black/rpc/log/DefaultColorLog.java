package com.black.rpc.log;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.util.StringUtils;
import com.black.core.util.TextUtils;
import com.black.utils.ServiceUtils;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

public class DefaultColorLog implements Log{

    public DefaultColorLog(){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
    }

    public static boolean INFO = true;
    public static boolean DEBUG = true;
    public static boolean ERROR = true;


    private String getCurrentInfo(){
        return StringUtils.linkStr(ServiceUtils.now("yyyy-MM-dd HH:mm:ss.sss"), " [", Thread.currentThread().getName(), "] ");
    }

    @Override
    public void info(String txt, Object... params) {
        if (INFO){
            String content = TextUtils.parseContent(txt, params);
            System.out.println(AnsiOutput.toString(AnsiColor.WHITE, getCurrentInfo(), AnsiColor.GREEN, content));
        }
    }

    @Override
    public void debug(String txt, Object... params) {
        if (DEBUG){
            String content = TextUtils.parseContent(txt, params);
            System.out.println(AnsiOutput.toString(AnsiColor.WHITE, getCurrentInfo(), AnsiColor.BRIGHT_YELLOW, content));
        }
    }

    @Override
    public void error(String txt, Object... params) {
        if (ERROR){
            String content = TextUtils.parseContent(txt, params);
            System.out.println(AnsiOutput.toString(AnsiColor.WHITE, getCurrentInfo(), AnsiColor.RED, content));
        }
    }

    @Override
    public void error(Throwable e) {
        CentralizedExceptionHandling.handlerException(e);
    }
}
