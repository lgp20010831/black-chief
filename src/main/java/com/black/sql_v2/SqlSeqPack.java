package com.black.sql_v2;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class SqlSeqPack {

    private final SqlType type;

    private final LinkedBlockingQueue<String> seqQueue = new LinkedBlockingQueue<>();

    private final Map<String, Object> keyValueMap = new LinkedHashMap<>();

    public SqlSeqPack(SqlType type) {
        this.type = type;
    }

    public void addSeq(String seq){
        seqQueue.add(seq);
    }

    public void addKeyAndValue(String column, Object value){
        keyValueMap.put(column, value);
    }
}
