package com.black.core.log;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SystemLoggor implements Logger{
    protected final PrintStream stream;
    protected final Class<?> clazz;
    protected final String className;
    public static final String FORMAT = "yyyy-MM-dd HH:mm:ss.sss";
    public SystemLoggor(Class<?> clazz){
        this.clazz = clazz;
        className = clazz.getName();
        stream = System.out;
    }

    @Override
    public void info(String txt, Object... params) {
        String info = parseContext(txt, "INFO", params);
        try {
            stream.write(info.getBytes());
            stream.flush();
        } catch (IOException e) {
            stream.println(info);
        }
    }

    @Override
    public void error(String txt, Object... params) {
        String info = parseContext(txt, "ERROR", params);
        stream.println(info);
    }

    @Override
    public void debug(String txt, Object params) {
        String info = parseContext(txt, "DEBUG", params);
        stream.println(info);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    protected String parseContext(String txt, String level, Object... params){
        StringBuilder builder = getStringBuidler(level);
        String[] sts = txt.split("\\{}");
        String[] ps = new String[sts.length];
        for (int i = 0; i < ps.length; i++) {
            if (i >= params.length){
                ps[i] = "";
            }else {
                ps[i] = params[i] == null ? "null" : params[i].toString();
            }
        }
        for (int i = 0; i < sts.length; i++) {
            String str = sts[i];
            builder.append(str);
            builder.append(ps[i]);
        }
        builder.append("\n");
        return builder.toString();
    }

    protected StringBuilder getStringBuidler(String level){
        StringBuilder builder = new StringBuilder();
        builder.append(new SimpleDateFormat(FORMAT).format(new Date()))
                .append(" [")
                .append(Thread.currentThread().getName())
                .append("] ")
                .append(level)
                .append(" ")
                .append(className)
                .append(" - ");
        return builder;
    }
}
