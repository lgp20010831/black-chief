package com.black.core.sql.code.cascade;

import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.SyntaxFactory;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.util.Assert;
import com.black.table.ColumnMetadata;
import com.black.table.ForeignKey;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Getter
public class CascadeGroup extends SyntaxFactory.SyntaxExecutor {

    //默认查询子表策略
    private Strategy strategy = Strategy.GROUP_BY;

    private boolean downward = false;

    //主表查询处理器
    private SyntaxFactory.SyntaxExecutor parent;

    private AliasColumnConvertHandler columnConvertHandler;

    private String suffix = "List";

    //策略处理器
    private static final LinkedBlockingQueue<StrategyCascadeExecutor> strategyCascadeExecutors = new LinkedBlockingQueue<>();

    private final Map<String, CascadeExecutor> subExecutors = new ConcurrentHashMap<>();

    static {
        strategyCascadeExecutors.add(new OneByOneStrategyExecutor());
        strategyCascadeExecutors.add(new GroupByStrategyExecutor());
    }

    private final Model model;

    public CascadeGroup(Model model){
        this.model = model;
    }


    public CascadeGroup strategy(Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public CascadeGroup callback(String name, Consumer<CascadeExecutor> consumer){
        CascadeExecutor executor = subExecutors.get(name);
        if (executor != null && consumer != null){
            consumer.accept(executor);
        }
        return this;
    }

    public CascadeGroup exclude(String... names){
        for (String name : names) {
            subExecutors.remove(name);
        }
        return this;
    }

    public CascadeGroup pointParent(SyntaxFactory.SyntaxExecutor parent){
        this.parent = parent;
        columnConvertHandler = parent.getMapping().getConvertHandler();
        if (model == Model.AUTO){
            autoFind();
        }
        return this;
    }

    public CascadeGroup select(String... names){
        Assert.notNull(parent, "parent is null");
        GlobalParentMapping mapping = parent.getMapping();
        String name = parent.getName();
        TableMetadata metadata = mapping.getMetadata(name);
        metadata.findSubset(mapping.getFetchConnection());
        for (String subName : names) {
            TableMetadata subMetadata = metadata.getSubMetadata(subName);
            if (subMetadata != null){
                ForeignKey foreignKey = getForeignKeyBySubMetadata(metadata, subMetadata);
                addSubExecutor(castForeignKeyToExecutor(foreignKey, parent));
            }
        }
        return this;
    }

    private ForeignKey getForeignKeyBySubMetadata(TableMetadata masterTable, TableMetadata subTable){
        PrimaryKey primaryKey = masterTable.firstPrimaryKey();
        return subTable.getForeignByPrimaryNameAndTableName(primaryKey.getName(), masterTable.getTableName());
    }

    private void autoFind(){
        Assert.notNull(parent, "parent is null");
        GlobalParentMapping mapping = parent.getMapping();
        String name = parent.getName();
        TableMetadata metadata = mapping.getMetadata(name);
        metadata.findSubset(mapping.getFetchConnection());
        for (TableMetadata subMetadata : metadata.getSubsetMetadataList()) {
            ForeignKey foreignKey = getForeignKeyBySubMetadata(metadata, subMetadata);
            addSubExecutor(castForeignKeyToExecutor(foreignKey, parent));
        }
    }

    public static CascadeExecutor castForeignKeyToExecutor(ForeignKey foreignKey, SyntaxFactory.SyntaxExecutor parent){
        CascadeExecutor cascadeExecutor = new CascadeExecutor();
        ColumnMetadata rawColumnMetadata = foreignKey.getRawColumnMetadata();
        TableMetadata rawTableMetadata = foreignKey.getRawTableMetadata();
        PrimaryKey mappingPrimaryKey = foreignKey.getMappingPrimaryKey();
        cascadeExecutor.name(rawTableMetadata.getTableName());
        cascadeExecutor.targetKey(rawColumnMetadata.getName());
        cascadeExecutor.targetName(rawTableMetadata.getTableName());
        cascadeExecutor.itselfKey(mappingPrimaryKey.getName());
        cascadeExecutor.mapper(parent.getMapping());
        return cascadeExecutor;
    }

    public CascadeGroup addSubExecutor(CascadeExecutor executor){
        subExecutors.put(executor.getName(), executor);
        return this;
    }

    public CascadeGroup downward(boolean downward) {
        this.downward = downward;
        return this;
    }

    public CascadeGroup suffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    @Override
    public CascadeGroup mapper(GlobalParentMapping parentMapping) {
        if (parent != null){
            parent.mapper(parentMapping);
        }
        return this;
    }

    @Override
    public CascadeGroup apply(String applySql) {
        if (parent != null){
            parent.apply(applySql);
        }
        return this;
    }

    @Override
    public CascadeGroup setConfigurer(SyntaxConfigurer configurer) {
        if (parent != null){
            parent.setConfigurer(configurer);
        }
        return this;
    }

    @Override
    public CascadeGroup dicts(String... syntaxs) {
        if (parent != null){
            parent.dicts(syntaxs);
        }
        return this;
    }

    @Override
    public CascadeGroup sequences(String... syntaxs) {
        if (parent != null){
            parent.sequences(syntaxs);
        }
        return this;
    }

    @Override
    public CascadeGroup sets(String... syntaxs) {
        if (parent != null){
            parent.sets(syntaxs);
        }
        return this;
    }

    @Override
    public CascadeGroup resultColumns(String... syntaxs) {
        if (parent != null){
            parent.resultColumns(syntaxs);
        }
        return this;
    }

    @Override
    public CascadeGroup blend(String syntaxs) {
        if (parent != null){
            parent.blend(syntaxs);
        }
        return this;
    }

    @Override
    public CascadeGroup name(String name) {
        if (parent != null){
            parent.name(name);
        }
        return this;
    }

    @Override
    public CascadeGroup validReferences(int validReferences) {
        if (parent != null){
            parent.validReferences(validReferences);
        }
        return this;
    }

    @Override
    public CascadeGroup condition(Map<String, Object> condition) {
        if (parent != null){
            parent.condition(condition);
        }
        return this;
    }

    @Override
    public CascadeGroup clear() {
        if (parent != null){
            parent.clear();
        }
        return this;
    }

    public List<Map<String, Object>> list(){
        Assert.notNull(parent, "parent is null");
        //获取主表数据
        List<Map<String, Object>> maps = parent.list();
        loop: for (CascadeExecutor executor : subExecutors.values()) {
            for (StrategyCascadeExecutor strategyCascadeExecutor : strategyCascadeExecutors) {
                if (strategyCascadeExecutor.support(strategy)) {
                    strategyCascadeExecutor.query(maps, this, executor);
                    continue loop;
                }
            }

        }
        return maps;
    }
}
