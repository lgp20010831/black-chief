package com.black.sql_v2.print;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;

import java.util.Collection;
import java.util.StringJoiner;

public class IoLogResultPrinter implements QueryResultPrinter{

    private final IoLog ioLog;

    public IoLogResultPrinter(){
        this(LogFactory.getArrayLog());
    }

    public IoLogResultPrinter(IoLog ioLog) {
        this.ioLog = ioLog;
    }


    @Override
    public void printEmpty() {
        ioLog.trace("===> empty result");
    }

    @Override
    public void printColumns(Collection<String> columns) {
        StringJoiner joiner = new StringJoiner(",  ", "===> ", ";");
        for (String column : columns) {
            joiner.add(column);
        }
        ioLog.trace(joiner.toString());
    }

    @Override
    public void printResultRow(Collection<Object> values) {
        StringJoiner joiner = new StringJoiner(",  ", "===> ", ";");
        for (Object val : values) {
            joiner.add(String.valueOf(val));
        }
        ioLog.trace(joiner.toString());
    }
}
