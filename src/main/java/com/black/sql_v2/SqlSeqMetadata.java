package com.black.sql_v2;

import com.black.core.sql.code.parse.BlendObject;
import com.black.core.sql.code.parse.CharParser;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SqlSeqMetadata {

    protected final Map<SqlType, SqlSeqPack> seqPackCache = new ConcurrentHashMap<>();

    public void registerSeq(SqlType type, String text){
        SqlSeqPack pack = seqPackCache.computeIfAbsent(type, SqlSeqPack::new);
        pack.addSeq(text);
    }

    public void registerKeyAndValue(SqlType type, String column, Object value){
        if (type == SqlType.INSERT_SET){
            registerKeyAndValue(SqlType.INSERT, column, value);
            registerKeyAndValue(SqlType.SET, column, value);
        }else {
            SqlSeqPack pack = seqPackCache.computeIfAbsent(type, SqlSeqPack::new);
            pack.addKeyAndValue(column, value);
        }
    }

    public void parseAndRegister(String blendTxt){
        List<BlendObject> blendObjects = CharParser.parseBlend(blendTxt);
        for (BlendObject blendObject : blendObjects) {
            String name = blendObject.getName();
            SqlType type;
            if ("where".equalsIgnoreCase(name)){
                type = SqlType.WHERE;
            }else if ("set".equalsIgnoreCase(name)){
                type = SqlType.SET;
            }else if ("insert".equalsIgnoreCase(name)){
                type = SqlType.INSERT;
            }else if ("insert_set".equalsIgnoreCase(name)){
                type = SqlType.INSERT_SET;
            }else {
                continue;
            }
            for (String attribute : blendObject.getAttributes()) {
                registerSeq(type, attribute);
            }
        }
    }


    public SqlSeqPack getPack(SqlType type){
        return seqPackCache.computeIfAbsent(type, SqlSeqPack::new);
    }

    public Map<SqlType, SqlSeqPack> getSeqPackCache() {
        return seqPackCache;
    }
}
