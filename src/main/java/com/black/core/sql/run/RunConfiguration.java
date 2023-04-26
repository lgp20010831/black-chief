package com.black.core.sql.run;

import com.black.core.json.ReflexUtils;
import com.black.core.sql.code.AliasColumnConvertHandler;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RunConfiguration {

    private String[] sqls;

    private boolean stopOnError;

    private boolean autoCommit;

    private Class<? extends AliasColumnConvertHandler> convertType;

    private AliasColumnConvertHandler handler;

    public void setConvertType(Class<? extends AliasColumnConvertHandler> convertType) {
        this.convertType = convertType;
        setHandler(ReflexUtils.instance(convertType));
    }
}
