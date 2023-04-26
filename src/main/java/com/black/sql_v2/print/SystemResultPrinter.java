package com.black.sql_v2.print;

import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.util.Collection;
import java.util.StringJoiner;

public class SystemResultPrinter implements QueryResultPrinter{
    @Override
    public void printEmpty() {
        System.out.println(wrapperWhiteMessage("===> empty result"));
    }

    @Override
    public void printColumns(Collection<String> columns) {
        StringJoiner joiner = new StringJoiner(",  ", "===> ", ";");
        for (String column : columns) {
            joiner.add(column);
        }
        System.out.println(wrapperWhiteMessage(joiner.toString()));
    }

    @Override
    public void printResultRow(Collection<Object> values) {
        StringJoiner joiner = new StringJoiner(",  ", "===> ", ";");
        for (Object val : values) {
            joiner.add(String.valueOf(val));
        }
        System.out.println(wrapperWhiteMessage(joiner.toString()));
    }

    public String wrapperWhiteMessage(String msg){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        return AnsiOutput.toString(AnsiColor.WHITE,  msg);
    }

}
