package com.black.core.sql.code.sqls;

import com.black.core.sql.unc.OperationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Getter @Setter
public class MappingVal {

    private OperationType operationType;
    private Object paramValue;
    private String columnName;

}
