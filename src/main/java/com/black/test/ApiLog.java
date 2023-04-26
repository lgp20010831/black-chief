package com.black.test;

import com.black.core.log.Catalog;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ApiLog implements Catalog {

    List<String> stack = new ArrayList<>();

    public ApiLog(){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
    }
    @Override
    public void info(String str) {
        stack.add(AnsiOutput.toString(AnsiColor.GREEN, str));
    }

    @Override
    public void debug(String str) {
        stack.add(AnsiOutput.toString(AnsiColor.YELLOW, str));
    }

    @Override
    public void trace(String msg) {
        stack.add(AnsiOutput.toString(AnsiColor.BRIGHT_BLUE, msg));
    }

    @Override
    public void error(String str) {
        stack.add(AnsiOutput.toString(AnsiColor.RED, str));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(stringStack().getBytes());
    }

    @Override
    public String stringStack() {
        StringBuilder builder = new StringBuilder();
        for (String msg : stack) {
            builder.append(msg);
        }
        return builder.toString();
    }

    @Override
    public void flush() {
        for (String msg : stack) {
            System.out.println(msg);
        }
        stack.clear();
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isEnabledInfo() {
        return true;
    }

    @Override
    public boolean isEnabledDebug() {
        return true;
    }

    @Override
    public boolean isEnabledTrace() {
        return true;
    }

    @Override
    public boolean isEnabledError() {
        return true;
    }
}
