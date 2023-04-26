package com.black.core.json;

import com.alibaba.fastjson.JSONArray;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


@QueueTail
public class DefaultTypeConvert implements ConversionFree {

    Date longToDate(Long data){
        return new Date(data);
    }

    Timestamp longToTimestamp(Long data){
        return new Timestamp(data);
    }

    Double longToDouble(Long data){
        return Double.parseDouble(data.toString());
    }

    String objectToString(Object obj){
        return obj.toString();
    }

    JSONArray listToJsonArray(List<Object> list){
        return new JSONArray(list);
    }

    Long strToLong(String data){return Long.valueOf(data);}

    Double strToDouble(String data){return Double.parseDouble(data);}

    Integer strToInt(String data) {return Integer.parseInt(data);}

    Boolean strToBool(String data){return Boolean.valueOf(data);}
}
