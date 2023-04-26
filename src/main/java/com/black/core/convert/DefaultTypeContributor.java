package com.black.core.convert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.json.JsonUtils;
import com.black.core.spring.instance.InstanceFactory;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.work.utils.WorkUtils;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
    Integer basicConvertInteger(@NonNull String param){
        return Integer.parseInt(param);
    }

    @ConversionWay
    Date basicConvertDate(String dateFormat) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateFormat);
    }

    @ConversionWay
    <T> JSONObject basicConvertJson(T pojo){
        return JsonUtils.toJson(pojo);
    }

    @ConversionWay
    String basicDataConvertToStr(Date time){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
    }

    @ConversionWay
    Double changeDouble(BigDecimal bigDecimal){
        return bigDecimal.doubleValue();
    }

    @ConversionWay
    Double intToDouble(Integer i){
        return new Double(i);
    }

    @ConversionWay
    int toInt(Integer i){
        return i;
    }

    @ConversionWay
    int toInt(String s){
        return s == null ? 0 : Integer.parseInt(s);
    }

    @ConversionWay
    int toInt(Double s){
        return s == null ? 0 : Integer.parseInt(s.toString());
    }

    @ConversionWay
    double toD(Double d){
        return d;
    }

    @ConversionWay
    long toLong(Long l){
        return l;
    }

    @ConversionWay
    short tos(String val){return toS(val);}

    @ConversionWay
    Short toS(String val){
        return Short.valueOf(val);
    }

    @ConversionWay
    float tof(String val){
        return toF(val);
    }

    @ConversionWay
    Float toF(String val){
        return Float.valueOf(val);
    }

    @ConversionWay
    double tod(String val){
        return toD(val);
    }

    @ConversionWay
    Double toD(String val){
        return Double.valueOf(val);
    }


    @ConversionWay
    Boolean toBol(String val){
        return toBool(val);
    }

    @ConversionWay
    boolean toBool(String value){
        return Boolean.parseBoolean(value);
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
    String conJsonToStr(JSONObject json){
        return json.toString();
    }

    @ConversionWay
    Float itf(Integer i){
        return i.floatValue();
    }

    @ConversionWay
    boolean toB(Boolean b){
        return b;
    }

    @ConversionWay
    Set<Object> toSs(Object obj){
        List<Object> list = SQLUtils.wrapList(obj);
        return new HashSet<>(list);
    }

}
