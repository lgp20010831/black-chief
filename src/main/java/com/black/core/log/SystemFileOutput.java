package com.black.core.log;

import com.black.io.out.DataByteBufferArrayOutputStream;

import java.io.*;

@SuppressWarnings("all")
public class SystemFileOutput implements LoggerOutput{

    private DataByteBufferArrayOutputStream fileOut;

    private SystemOutput systemOutput;

    public SystemFileOutput(File file){
        systemOutput = new SystemOutput();
        if (file != null){
            try {
                fileOut = new DataByteBufferArrayOutputStream(new FileOutputStream(file));
            } catch (FileNotFoundException e) {
                //ignore
            }
        }
    }

    @Override
    public void println(String msg, LogLevel logLevel) {
        systemOutput.println(msg, logLevel);
        if (fileOut != null){
            try {
                String ws = wrapperMsg(msg, logLevel);
                fileOut.write(ws.getBytes("UTF-8"));
            } catch (IOException e) {
                //ignore
            }
        }
    }

    private String wrapperMsg(String msg, LogLevel logLevel){
        if (logLevel == LogLevel.THROWABLE){
            return msg + "\n";
        }else {
            return LogUtils.getCurrentInfo() + msg + "\n";
        }
    }

    @Override
    public void flush() {
        systemOutput.flush();
        if (fileOut != null){
            try {
                fileOut.flush();
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
