package com.black.sql_v2.with;

import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;
import lombok.Data;

@Data
public class WaitGenerateWrapper {

    private Object bean;

    private AliasColumnConvertHandler convertHandler;

    private final String passId;

    private final String tableName;

    public WaitGenerateWrapper(Object bean, String passId, String tableName) {
        this.bean = bean;
        this.passId = passId;
        this.tableName = tableName;
    }

    public WaitGenerateWrapper(Object bean, AliasColumnConvertHandler convertHandler, String passId, String tableName) {
        this.bean = bean;
        this.convertHandler = convertHandler;
        this.passId = passId;
        this.tableName = tableName;
    }

    public AliasColumnConvertHandler getConvertHandler() {
        if (convertHandler == null)
            convertHandler = new HumpColumnConvertHandler();
        return convertHandler;
    }
}
