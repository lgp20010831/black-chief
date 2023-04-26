package com.black.core.log;

import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.io.PrintStream;

import static com.black.core.log.LogUtils.getCurrentInfo;

public class SystemOutput implements LoggerOutput{

    PrintStream printStream;

    public SystemOutput(){
        printStream = System.out;
        openColor();
    }

    private void openColor(){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
    }

    @Override
    public void println(String msg, LogLevel logLevel) {
        String level = logLevel.getLevel();
        AnsiColor color = ArrayIoLog.getColor(level);
        String wrapperString = wrapperString(color, msg, logLevel);
        printStream.println(wrapperString);
    }

    private String wrapperString(AnsiColor color, String msg, LogLevel level){
        if (level == LogLevel.THROWABLE){
            return msg;
        }
        return AnsiOutput.toString(AnsiColor.WHITE, getCurrentInfo(), color, msg);
    }

    @Override
    public void flush() {

    }
}
