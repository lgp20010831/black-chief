package com.black.sql_v2;

import com.black.json.JsonParser;
import com.black.core.log.IoLog;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.config.StatementValueSetDisplayConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Environment extends SqlSeqMetadata{

    private final GlobalEnvironment parent;

    private DataSourceBuilder dataSourceBuilder;

    private IoLog log;

    private JsonParser jsonParser;

    private int insertBatch;

    private AliasColumnConvertHandler convertHandler;

    private StatementValueSetDisplayConfiguration displayConfiguration;

    public Environment(GlobalEnvironment parent) {
        this.parent = parent;
        seqPackCache.putAll(parent.getSeqPackCache());
    }

    public void clear(){
        seqPackCache.clear();
    }
}
