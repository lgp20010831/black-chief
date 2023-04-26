package com.black.core.log;

import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

public class IbatisLog extends StdOutImpl {

    public static boolean ignoreOther = true;

    public static boolean ignoreTrace = false;

    public IbatisLog(String clazz) {
        super(clazz);
    }

    @Override
    public void debug(String s) {
        if (s != null && s.startsWith("==>")){
            System.out.println(AnsiOutput.toString(AnsiColor.GREEN, s));
        }else if (!ignoreOther){
            System.out.println(s);
        }
    }

    @Override
    public void trace(String s) {
        if(!ignoreTrace){
            System.out.println(AnsiOutput.toString(AnsiColor.WHITE, s));
        }
    }
}
