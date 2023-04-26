package com.black.sql_v2.print;

import java.util.Collection;

public interface QueryResultPrinter {

    void printEmpty();

    void printColumns(Collection<String> columns);

    void printResultRow(Collection<Object> values);
}
