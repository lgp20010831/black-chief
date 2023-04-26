package com.black.sql;


import com.black.core.sql.unc.OperationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Getter @Setter @AllArgsConstructor
public class Operation {


    private Consumer<Operation> consumer;
    private String columnName, value;
    private boolean type;
    private OperationType operationType;
    private SqlOutStatement othereStatement;

    public Operation(Consumer<Operation> consumer) {
        this.consumer = consumer;
    }

    public Operation( Consumer<Operation> consumer, String columnName) {

        this.consumer = consumer;
        this.columnName = columnName;
    }

    public Operation(Consumer<Operation> consumer, String columnName, String value, boolean type) {
        this.consumer = consumer;
        this.columnName = columnName;
        this.value = value;
        this.type = type;
    }
}
