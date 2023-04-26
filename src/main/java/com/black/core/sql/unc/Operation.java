package com.black.core.sql.unc;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Getter @Setter @AllArgsConstructor
public class Operation {

    private int sort;
    private Consumer<Operation> consumer;
    private String columnName, value;
    private boolean type;
    private OperationType operationType;
    private SqlStatement othereStatement;

    public Operation(int sort, Consumer<Operation> consumer) {
        this.sort = sort;
        this.consumer = consumer;
    }

    public Operation(int sort, Consumer<Operation> consumer, String columnName) {
        this.sort = sort;
        this.consumer = consumer;
        this.columnName = columnName;
    }

    public Operation(int sort, Consumer<Operation> consumer, String columnName, String value, boolean type) {
        this.sort = sort;
        this.consumer = consumer;
        this.columnName = columnName;
        this.value = value;
        this.type = type;
    }
}
