package com.black.core.convert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.json.JsonUtils;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.work.utils.WorkUtils;
import com.black.utils.TypeUtils;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Log4j2
@TypeContributor
public class DefaultTypeContributor {

    private final InstanceFactory instanceFactory;
    public static final String JSON_TYPE_VU = "VU:";
    public static final String JSON_TYPE_UC = "UC:";

    public DefaultTypeContributor(InstanceFactory instanceFactory) {
        this.instanceFactory = instanceFactory;
    }

    @ConversionWay
    Timestamp objCastToTimestamp(Object obj){
        return TypeUtils.castToTimestamp(obj);
    }

    @ConversionWay
    Integer basicConvertInteger(@NonNull Object param){
        return TypeUtils.castToInt(param);
    }

    @ConversionWay
    Date basicConvertDate(Object dateFormat) throws ParseException {
        return TypeUtils.castToDate(dateFormat);
    }

    @ConversionWay
    JSONObject basicConvertJson(Object pojo){
        return JsonUtils.toJson(pojo);
    }

    @ConversionWay
    String basicDataConvertToStr(Date time){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
    }

    @ConversionWay
    Double changeDouble(Object val){
        return TypeUtils.castToDouble(val);
    }


    @ConversionWay
    Long toLong(Object l){
        return TypeUtils.castToLong(l);
    }

    @ConversionWay
    Short toS(Object val){
        return TypeUtils.castToShort(val);
    }

    @ConversionWay
    Float tof(Object val){
        return TypeUtils.castToFloat(val);
    }

    @ConversionWay
    Boolean toBol(Object val){
        return TypeUtils.castToBoolean(val);
    }

    @ConversionWay(priority = 100)
    JSONObject parseObject(String text){
        if (!StringUtils.hasText(text)){
            return new JSONObject();
        }
        if (text.startsWith(JSON_TYPE_VU)){
            return JsonUtils.vuParseJson(text);
        }else if (text.startsWith(JSON_TYPE_UC)){
            int i = text.indexOf(JSON_TYPE_UC);
            return JsonUtils.ucParseJson(text.substring(i + JSON_TYPE_UC.length()));
        }else {
            return WorkUtils.parseObject(text);
        }
    }

    @ConversionWay
    JSONArray parseArray(String text){
        return WorkUtils.parseArray(text);
    }

    @ConversionWay
    String parseList(List<Object> list){
        return new JSONArray(list).toString();
    }

    @ConversionWay
    List<String> parseString(String text){
        return WorkUtils.convertList(JSON.parseArray(text));
    }

    @ConversionWay
    Set<Object> toSs(Object obj){
        List<Object> list = SQLUtils.wrapList(obj);
        return new HashSet<>(list);
    }

    @ConversionWay
    LinkedHashMap<String, Object> toLinkMap(Object obj){
        return new LinkedHashMap<>(JsonUtils.letJson(obj));
    }

}
