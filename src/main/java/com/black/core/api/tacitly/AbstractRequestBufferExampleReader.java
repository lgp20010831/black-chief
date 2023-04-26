package com.black.core.api.tacitly;

import com.black.core.builder.Col;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractRequestBufferExampleReader {

    public static final String LIST_REQUEST_FLAG = "List";

    public static final String SPACE = "\u3000";
    protected final ApiAliasManger aliasManger;

    protected final AnalysisProcessActuator processActuator;
    public AbstractRequestBufferExampleReader(ApiAliasManger aliasManger, AnalysisProcessActuator processActuator) {
        this.aliasManger = aliasManger;
        this.processActuator = processActuator;
    }

    public String writeStream(Map<String, String> params){
        if (params == null || params.isEmpty()){
            return "{}";
        }
        StringBuilder builder = new StringBuilder();
        int jsonStart = writeJsonStart(builder, 0);
        try {
            params.forEach((name, type) ->{
                int offSet = writeKey(name, builder, jsonStart);
                writeColon(builder, 0);
                if (type.startsWith(LIST_REQUEST_FLAG)){
                    offSet = offSet + writeListStart(builder, 0);
                    try {

                        String listParam = type.substring(4);
                        if (listParam.startsWith("-")){
                            String aliases = listParam.substring(1);
                            String[] aliasArray = aliases.split(",");
                            Map<String, String> map = new HashMap<>();
                            for (String alias : aliasArray) {
                                Class<?> pojo = aliasManger.queryPojo(alias);
                                if (pojo == null){
                                    throw new RuntimeException("无法找到依赖的实体类对象:" + alias);
                                }
                                map.putAll(processActuator.queryParamMap(pojo));
                            }
                            offSet = writeJsonStart(builder, offSet);
                            builder.append(writeParams(map, offSet));
                            writeJsonEnd(builder, offSet - 1);
                        }else {
                            writeValue("String", builder, offSet);
                        }
                    }finally {
                        writeListEnd(builder, offSet);
                    }
                }else{
                    Class<?> pojo = aliasManger.queryPojo(type);
                    if (pojo != null){
                        Map<String, String> map = processActuator.queryParamMap(pojo);
                        try {
                            offSet = offSet + writeJsonStart(builder, 0);
                            writeParams(map, offSet);
                        }finally {
                            writeJsonEnd(builder, offSet);
                        }
                    }else {
                        writeValue(type, builder, 0);
                        writeComma(builder);
                    }
                }
            });
        }finally {
            writeJsonEnd(builder, 0);
        }
        return builder.toString();
    }

    protected int writeJsonStart(StringBuilder builder, int offset){
        writeOff(builder, offset);
        builder.append("{\n");
        return offset + 1;
    }

    protected int writeJsonEnd(StringBuilder builder, int offSet){
        writeOff(builder, offSet);
        builder.append("}\n");
        return 1;
    }

    protected int writeListStart(StringBuilder builder, int offSet){
        writeOff(builder, offSet);
        builder.append("[\n");
        return offSet + 1;
    }

    protected int writeListEnd(StringBuilder builder, int offSet){
        writeOff(builder, offSet);
        builder.append(", ...]\n");
        return 1;
    }

    protected void writeOff(StringBuilder builder, int loop){
        for (int i = 0; i < loop; i++) {
            builder.append(" ");
        }
    }

    protected void writeStart(StringBuilder builder){
        builder.append("{\n");
    }

    protected void writeEnd(StringBuilder builder){
        builder.append("\n}");
    }

    protected void writeListStart(StringBuilder builder){builder.append("[\n");}

    protected void writeListEnd(StringBuilder builder){builder.append(", ... ]");}

    protected void writeComma(StringBuilder builder){
        builder.append(",\n");
    }

    protected void writeEnter(StringBuilder builder){
        builder.append("\n");
    }

    protected int writeKey(String key, StringBuilder builder, int offSet){
        writeOff(builder, offSet);
        builder.append(key);
        return offSet + key.length() + 1;
    }

    protected int writeColon(StringBuilder builder, int offSet){
        writeOff(builder, offSet);
        builder.append(":");
        return offSet + 1;
    }
    protected int writeValue(String valueType, StringBuilder builder, int offSet){
        writeOff(builder, offSet);
        if (Col.or(valueType, Col.ar("int", "Integer", "long", "Long"))){
            builder.append(1);
            return offSet + 1;
        }else if (Col.or(valueType, Col.ar("Boolean", "boolean", "b", "bit"))){
            builder.append("true");
            return offSet + 4;
        }else {
            builder.append("xxxxxxx");
            return offSet + 7;
        }
    }
    protected String writeParams(Map<String, String> params, int offSet){
        StringBuilder builder = new StringBuilder();
        AtomicInteger index = new AtomicInteger(params.size());
        params.forEach((name, type) ->{
            builder.append(writeParam(name, type, offSet, index.get() == 1));
            index.decrementAndGet();
        });

        return builder.toString();
    }

    protected String writeParam(String name, String type, int offSet, boolean end){
        StringBuilder builder = new StringBuilder();
        int keyOffSet = writeKey(name, builder, offSet);
        writeColon(builder, 0);
        if ("code".equals(name)){
            builder.append(200);
        }else {
            writeValue(type, builder, 0);
        }
        if (!end){
            writeComma(builder);
        }else {
            writeEnter(builder);
        }
        return builder.toString();
    }

}
