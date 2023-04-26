package com.black.core.sql.code.log;

import com.black.core.log.LogUtils;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

public class SystemLog implements Log {

    public SystemLog(){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
    }

    public static boolean info = true;

    public static boolean debug = true;

    public static boolean error = true;

    @Override
    public boolean isDebugEnabled() {
        return debug;
    }

    @Override
    public boolean isErrorEnabled() {
        return error;
    }

    @Override
    public boolean isInfoEnabled() {
        return info;
    }

    @Override
    public void info(String str) {
        if (str != null && str.startsWith("===>> ")){
            System.out.println(wrapper(AnsiColor.WHITE, str));
        }
    }

    @Override
    public void debug(String str) {
        if (str != null && str.startsWith("==>")){
            System.out.println(wrapper(AnsiColor.GREEN, str));
        }else if (str != null && str.startsWith("<==")){
            System.out.println(wrapper(AnsiColor.BRIGHT_RED, str));
        }else if (str != null && str.startsWith("-->")){
            System.out.println(wrapper(AnsiColor.BRIGHT_YELLOW, str));
        }
    }

    @Override
    public void error(String str) {
        System.err.println(str);
    }

    private String wrapper(AnsiColor color, String str){
        return AnsiOutput.toString(AnsiColor.WHITE, LogUtils.getCurrentInfo(), color, str);
    }
}
