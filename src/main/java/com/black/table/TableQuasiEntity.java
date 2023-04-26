package com.black.table;

import com.black.core.util.Body;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TableQuasiEntity implements Map<String, Object>{

    private final TableMetadata metadata;

    private final Map<String, Object> resultMap = new HashMap<>();

    public TableQuasiEntity(TableMetadata metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    public void putResult(String columnName, Object result){
        resultMap.put(columnName, result);
    }

    public Body getResultBody(){
        return new Body(getResultMap());
    }

    public Object getResult(String columnName){
        return resultMap.get(columnName);
    }

    public TableMetadata getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "TableQuasiEntity[" +
                "resultMap=" + resultMap +
                ']';
    }

    @Override
    public int size() {
        return resultMap.size();
    }

    @Override
    public boolean isEmpty() {
        return resultMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return resultMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return resultMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return resultMap.get(key);
    }

    @Nullable
    @Override
    public Object put(String key, Object value) {
        return resultMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return resultMap.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> m) {
        resultMap.putAll(m);
    }

    @Override
    public void clear() {
        resultMap.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return resultMap.keySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return resultMap.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return resultMap.entrySet();
    }
}
