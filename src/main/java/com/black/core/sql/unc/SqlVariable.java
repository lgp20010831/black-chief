package com.black.core.sql.unc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Getter @Setter
public class SqlVariable {

    private int index;
    private String columnName;
    private OperationType operationType;

}
