package com.black.core.log;

import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

public class SysLog$_8j extends System9vLog implements Log$8$j{

    public SysLog$_8j(){
        this(Thread.currentThread().getClass());
    }

    public SysLog$_8j(Class<?> clazz) {
        super(clazz);
    }

    @Override
    public void fierce(String txt, Object... param) {
        String fierce = parseContext(txt, "FIERCE", param);
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        stream.println(AnsiOutput.toString(AnsiColor.RED, fierce));
    }
}
