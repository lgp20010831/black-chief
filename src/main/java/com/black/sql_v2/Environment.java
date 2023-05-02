package com.black.sql_v2;

import com.black.core.log.IoLog;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.config.StatementValueSetDisplayConfiguration;
import com.black.json.JsonParser;
import com.black.sql_v2.utils.VarcharIdType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Environment extends SqlSeqMetadata{

    private final GlobalEnvironment parent;

    private DataSourceBuilder dataSourceBuilder;

    private IoLog log;

    private int increasingRandomRange = 1000 * 1000 * 1000;

    private boolean autoSetId = true;

    private VarcharIdType varcharIdType = VarcharIdType.UUID;

    private JsonParser jsonParser;

    private int insertBatch;

    private boolean useEnhanceSerializer = true;

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
