package com.black.role.impl.def;

import com.black.core.sql.code.log.Log;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

public class DefaultLog implements Log {
    public DefaultLog(){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String str) {
        if (str != null){
            if (str.startsWith("[token creator] ==>")){
                System.out.println(AnsiOutput.toString(AnsiColor.RED, str));
            }else if (str.startsWith("[token resolver]")){
                System.out.println(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, str));
            }
        }
    }

    @Override
    public void debug(String str) {
        if (str != null){
            if (str.startsWith("[token intercept] ==>")){
                System.out.println(AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW, str));
            }else if (str.startsWith("[cross] ==>")){
                System.out.println(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, str));
            }else if (str.startsWith("[token intercept] <==")){
                System.out.println(AnsiOutput.toString(AnsiColor.RED, str));
            }else if (str.startsWith("[token pass] ==>")){
                System.out.println(AnsiOutput.toString(AnsiColor.GREEN, str));
            }else if (str.startsWith("[token entity] ==>")){
                System.out.println(AnsiOutput.toString(AnsiColor.GREEN, str));
            }else if (str.startsWith("[token match] ==>")){
                System.out.println(AnsiOutput.toString(AnsiColor.WHITE, str));
            }
        }
    }

    @Override
    public void error(String str) {
        if (str != null){
            System.out.println(AnsiOutput.toString(AnsiColor.RED, str));
        }
    }
}
