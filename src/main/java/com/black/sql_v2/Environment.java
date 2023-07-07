package com.black.sql_v2;

import com.black.core.log.IoLog;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.config.StatementValueSetDisplayConfiguration;
import com.black.datasource.DataSourceBuilderTypeManager;
import com.black.json.JsonParser;
import com.black.sql_v2.utils.VarcharIdType;
import com.black.utils.LocalSet;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Set;

@Getter @Setter
public class Environment extends SqlSeqMetadata{

    private GlobalEnvironment parent;

    private DataSourceBuilder dataSourceBuilder;

    private Class<? extends DataSourceBuilder> builderClass;

    private IoLog log;

    private int increasingRandomRange = 1000 * 1000 * 1000;

    private boolean autoSetId = true;

    private boolean enabledGeneratePrimary = false;

    private VarcharIdType varcharIdType = VarcharIdType.UUID;

    private JsonParser jsonParser;

    private int insertBatch;

    private boolean useEnhanceSerializer = true;

    private AliasColumnConvertHandler convertHandler;

    private StatementValueSetDisplayConfiguration displayConfiguration;

    private final LocalSet<String> memorySelectTableNames = new LocalSet<>();

    private final ThreadLocal<String> schemaLocal = new ThreadLocal<>();

    private boolean openMemorySelect = false;

    public Environment() {
        this.parent = GlobalEnvironment.getInstance();
        seqPackCache.putAll(parent.getSeqPackCache());
    }

    public void clear(){
        seqPackCache.clear();
    }

    public void setBuilderClass(Class<? extends DataSourceBuilder> builderClass) {
        this.builderClass = builderClass;
        dataSourceBuilder = DataSourceBuilderTypeManager.getBuilder(builderClass);
    }

    public DataSourceBuilder getDataSourceBuilder() {
        if (dataSourceBuilder == null){
            dataSourceBuilder = parent.getDataSourceBuilder();
        }
        return dataSourceBuilder;
    }

    public Set<String> getMemoryTables(){
        return memorySelectTableNames.current();
    }

    public void registerMemoryTables(String... tables){
        memorySelectTableNames.addAll(Arrays.asList(tables));
    }

    public void clearMemoryTables(){
        memorySelectTableNames.clear();
    }

    public String getCurrentSchame(){
        return schemaLocal.get();
    }

    public void setSchemaLocal(String schema){
        schemaLocal.set(schema);
    }

    public void clearSchema(){
        schemaLocal.remove();
    }
}
