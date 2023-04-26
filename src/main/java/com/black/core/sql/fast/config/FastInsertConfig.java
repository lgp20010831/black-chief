package com.black.core.sql.fast.config;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FastInsertConfig extends Configuration {

    Integer batchSize;

    boolean parseResult = false;

    public FastInsertConfig(GlobalSQLConfiguration configuration, MethodWrapper mw) {
        super(configuration, mw);
    }


}
