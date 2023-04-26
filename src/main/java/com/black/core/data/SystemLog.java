package com.black.core.data;

import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

public class SystemLog implements DataLog{

    public SystemLog(){
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
        System.out.println(AnsiOutput.toString(AnsiColor.BLUE, str));
    }

    @Override
    public void debug(String str) {
        System.out.println(AnsiOutput.toString(AnsiColor.BRIGHT_GREEN, str));
    }

    @Override
    public void error(String str) {
        System.out.println(AnsiOutput.toString(AnsiColor.RED, str));
    }
}
