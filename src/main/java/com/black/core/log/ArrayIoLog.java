package com.black.core.log;

import com.black.io.in.DataByteBufferArrayInputStream;
import com.black.io.out.DataByteBufferArrayOutputStream;
import com.black.core.util.ExceptionUtil;
import com.black.core.util.TextUtils;
import org.springframework.boot.ansi.AnsiColor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ArrayIoLog extends AbstractIoLog{

    private boolean delay = false;

    private boolean close = false;

    public static final String INFO = "INFO";
    public static final String DEBUG = "DEBUG";
    public static final String TRACE = "TRACE";
    public static final String ERROR = "ERROR";
    public static Map<String, AnsiColor> colorTable = new ConcurrentHashMap<>();
    static {
        colorTable.put(INFO, AnsiColor.GREEN);
        colorTable.put(DEBUG, AnsiColor.BRIGHT_BLUE);
        colorTable.put(TRACE, AnsiColor.BRIGHT_YELLOW);
        colorTable.put(ERROR, AnsiColor.RED);
    }

    public static AnsiColor getColor(String key){
        AnsiColor color = colorTable.get(key);
        return color == null ? AnsiColor.BLACK : color;
    }

    private final LoggerOutput output;

    private final LinkedBlockingQueue<LogMsg> msgArrayList = new LinkedBlockingQueue<>();

    public ArrayIoLog(File file){
        output = new SystemFileOutput(file);
    }

    public ArrayIoLog(){
        output = new SystemOutput();
    }

    private int push(String msg, LogLevel level){
        LogMsg logMsg = new LogMsg(level);
        logMsg.setMsg(msg);
        try {
            msgArrayList.put(logMsg);
        } catch (InterruptedException e) {}
        if (!delay){
            return flush() > 0 ? 1 : 0;
        }
        return 0;
    }

    private String getMsg(Object obj){
        return (prefix == null ? "" : prefix) + (obj == null ? "null" : obj.toString());
    }

    @Override
    public int info(Object msg, Object... params) {
        if (isInfoEnabled()){
            String content = TextUtils.parseContent(getMsg(msg), params);
            return push(content, LogLevel.INFO);
        }
        return 0;
    }

    @Override
    public int debug(Object msg, Object... params) {
        if (isDebugEnabled()){
            String content = TextUtils.parseContent(getMsg(msg), params);
            return push(content, LogLevel.DEBUG);
        }
        return 0;
    }

    @Override
    public int trace(Object msg, Object... params) {
        if (isTraceEnabled()){
            String content = TextUtils.parseContent(getMsg(msg), params);
            return push(content, LogLevel.TRACE);
        }
        return 0;
    }

    @Override
    public int error(Throwable e, Object msg, Object... params) {
        if (e != null){
            String stackTraceInfo = ExceptionUtil.getStackTraceInfo(e);
            push(stackTraceInfo, LogLevel.THROWABLE);
        }
        if (isErrorEnabled()){
            String content = TextUtils.parseContent(getMsg(msg), params);
            return push(content, LogLevel.ERROR);
        }
        return 1;
    }

    @Override
    public int flush(int index) {
        if (index == -1){
            return flushAll();
        }
        synchronized (msgArrayList){
            LogMsg msg = msgArrayList.poll();
            writeMsg(msg);
            flush0();
            return 1;
        }
    }

    @Override
    public void enabledDelay(boolean delay) {
        this.delay = delay;
    }

    @Override
    public void close() {
        close = true;
        msgArrayList.clear();
    }

    @Override
    public DataByteBufferArrayInputStream getInputStream() throws IOException {
        String stack = stringStack();
        DataByteBufferArrayOutputStream out = new DataByteBufferArrayOutputStream();
        out.writeUnrestrictedUtf(stack);
        return new DataByteBufferArrayInputStream(out.toByteArray());
    }

    @Override
    public String stringStack() {
        StringJoiner builder = new StringJoiner("\n");
        synchronized (msgArrayList){
            for (LogMsg msg : msgArrayList) {
                builder.add(msg.getMsg());
            }
            return builder.toString();
        }
    }

    private int flushAll(){
        synchronized (msgArrayList){
            int size = msgArrayList.size();
            for (LogMsg msg : msgArrayList) {
                writeMsg(msg);
            }
            flush0();
            msgArrayList.clear();
            return size;
        }
    }

    private void writeMsg(LogMsg msg){
        if (!close){
            doWrite0(msg.getMsg(), msg.getLogLevel());
        }
    }

    private void flush0(){
        output.flush();
    }

    private void doWrite0(String msg, LogLevel level){
        output.println(msg, level);
    }


}
