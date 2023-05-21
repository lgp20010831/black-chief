package com.black.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.EnumDeserializer;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.CalendarCodec;
import com.alibaba.fastjson.util.IOUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
//cop by fastjson
public class TypeUtilsByConvert {
    private static final Pattern NUMBER_WITH_TRAILING_ZEROS_PATTERN = Pattern.compile("\\.0*$");

    private static boolean oracleTimestampMethodInited = false;
    private static Method oracleTimestampMethod;
    private static boolean oracleDateMethodInited = false;
    private static Method oracleDateMethod;
    private static volatile Class class_Clob = null;
    private static volatile boolean class_Clob_error = false;

    private static volatile Class class_XmlAccessType = null;
    private static volatile Class class_XmlAccessorType = null;
    private static volatile boolean classXmlAccessorType_error = false;
    private static volatile Method method_XmlAccessorType_value = null;
    private static volatile Field field_XmlAccessType_FIELD = null;
    private static volatile Object field_XmlAccessType_FIELD_VALUE = null;

    private static Class class_deque = null;

    static {
        try {
            TypeUtils.compatibleWithJavaBean = "true".equals(IOUtils.getStringProperty(IOUtils.FASTJSON_COMPATIBLEWITHJAVABEAN));
            TypeUtils.compatibleWithFieldName = "true".equals(IOUtils.getStringProperty(IOUtils.FASTJSON_COMPATIBLEWITHFIELDNAME));
        } catch (Throwable e) {
            // skip
        }

        try {
            class_deque = Class.forName("java.util.Deque");
        } catch (Throwable e) {
            // skip
        }
    }

    public static boolean isXmlField(Class clazz) {
        if (class_XmlAccessorType == null && !classXmlAccessorType_error) {
            try {
                class_XmlAccessorType = Class.forName("javax.xml.bind.annotation.XmlAccessorType");
            } catch(Throwable ex){
                classXmlAccessorType_error = true;
            }
        }

        if (class_XmlAccessorType == null) {
            return false;
        }

        Annotation annotation = TypeUtils.getAnnotation(clazz, class_XmlAccessorType);
        if (annotation == null) {
            return false;
        }

        if (method_XmlAccessorType_value == null && !classXmlAccessorType_error) {
            try {
                method_XmlAccessorType_value = class_XmlAccessorType.getMethod("value");
            } catch(Throwable ex){
                classXmlAccessorType_error = true;
            }
        }

        if (method_XmlAccessorType_value == null) {
            return false;
        }

        Object value = null;
        if (!classXmlAccessorType_error) {
            try {
                value = method_XmlAccessorType_value.invoke(annotation);
            } catch (Throwable ex) {
                classXmlAccessorType_error = true;
            }
        }
        if (value == null) {
            return false;
        }

        if (class_XmlAccessType == null && !classXmlAccessorType_error) {
            try {
                class_XmlAccessType = Class.forName("javax.xml.bind.annotation.XmlAccessType");
                field_XmlAccessType_FIELD = class_XmlAccessType.getField("FIELD");
                field_XmlAccessType_FIELD_VALUE = field_XmlAccessType_FIELD.get(null);
            } catch(Throwable ex){
                classXmlAccessorType_error = true;
            }
        }

        return value == field_XmlAccessType_FIELD_VALUE;
    }

    public static Annotation getXmlAccessorType(Class clazz) {
        if (class_XmlAccessorType == null && !classXmlAccessorType_error) {

            try{
                class_XmlAccessorType = Class.forName("javax.xml.bind.annotation.XmlAccessorType");
            } catch(Throwable ex){
                classXmlAccessorType_error = true;
            }
        }

        if (class_XmlAccessorType == null) {
            return null;
        }

        return  TypeUtils.getAnnotation(clazz, class_XmlAccessorType);
    }

//
//    public static boolean isXmlAccessType(Class clazz) {
//        if (class_XmlAccessType == null && !class_XmlAccessType_error) {
//
//            try{
//                class_XmlAccessType = Class.forName("javax.xml.bind.annotation.XmlAccessType");
//            } catch(Throwable ex){
//                class_XmlAccessType_error = true;
//            }
//        }
//
//        if (class_XmlAccessType == null) {
//            return false;
//        }
//
//        return  class_XmlAccessType.isAssignableFrom(clazz);
//    }

    public static boolean isClob(Class clazz) {
        if (class_Clob == null && !class_Clob_error) {

            try{
                class_Clob = Class.forName("java.sql.Clob");
            } catch(Throwable ex){
                class_Clob_error = true;
            }
        }

        if (class_Clob == null) {
            return false;
        }

        return  class_Clob.isAssignableFrom(clazz);
    }

    public static String castToString(Object value){
        if(value == null){
            return null;
        }
        return value.toString();
    }

    public static Byte castToByte(Object value){
        if(value == null){
            return null;
        }

        if(value instanceof BigDecimal){
            return byteValue((BigDecimal) value);
        }

        if(value instanceof Number){
            return ((Number) value).byteValue();
        }

        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            return Byte.parseByte(strVal);
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? (byte) 1 : (byte) 0;
        }

        throw new JSONException("can not cast to byte, value : " + value);
    }

    public static Character castToChar(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Character){
            return (Character) value;
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0){
                return null;
            }
            if(strVal.length() != 1){
                throw new JSONException("can not cast to char, value : " + value);
            }
            return strVal.charAt(0);
        }
        throw new JSONException("can not cast to char, value : " + value);
    }

    public static Short castToShort(Object value){
        if(value == null){
            return null;
        }

        if(value instanceof BigDecimal){
            return shortValue((BigDecimal) value);
        }

        if(value instanceof Number){
            return ((Number) value).shortValue();
        }

        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            return Short.parseShort(strVal);
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? (short) 1 : (short) 0;
        }

        throw new JSONException("can not cast to short, value : " + value);
    }

    public static BigDecimal castToBigDecimal(Object value){
        if (value == null) {
            return null;
        }

        if (value instanceof Float) {
            if (Float.isNaN((Float) value) || Float.isInfinite((Float) value)) {
                return null;
            }
        } else if (value instanceof Double) {
            if (Double.isNaN((Double) value) || Double.isInfinite((Double) value)) {
                return null;
            }
        } else if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        } else if (value instanceof Map && ((Map) value).size() == 0) {
            return null;
        }

        String strVal = value.toString();

        if (strVal.length() == 0
                || strVal.equalsIgnoreCase("null")) {
            return null;
        }

        if (strVal.length() > 65535) {
            throw new JSONException("decimal overflow");
        }
        return new BigDecimal(strVal);
    }

    public static BigInteger castToBigInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Float) {
            Float floatValue = (Float) value;
            if (Float.isNaN(floatValue) || Float.isInfinite(floatValue)) {
                return null;
            }
            return BigInteger.valueOf(floatValue.longValue());
        } else if (value instanceof Double) {
            Double doubleValue = (Double) value;
            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                return null;
            }
            return BigInteger.valueOf(doubleValue.longValue());
        } else if (value instanceof BigInteger) {
            return (BigInteger) value;
        } else if (value instanceof BigDecimal) {
            BigDecimal decimal = (BigDecimal) value;
            int scale = decimal.scale();
            if (scale > -1000 && scale < 1000) {
                return ((BigDecimal) value).toBigInteger();
            }
        }

        String strVal = value.toString();

        if (strVal.length() == 0
                || strVal.equalsIgnoreCase("null")) {
            return null;
        }

        if (strVal.length() > 65535) {
            throw new JSONException("decimal overflow");
        }
        return new BigInteger(strVal);
    }

    public static Float castToFloat(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).floatValue();
        }
        if(value instanceof String){
            String strVal = value.toString();
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != -1){
                strVal = strVal.replaceAll(",", "");
            }
            return Float.parseFloat(strVal);
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? 1F : 0F;
        }

        throw new JSONException("can not cast to float, value : " + value);
    }

    public static Double castToDouble(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Number){
            return ((Number) value).doubleValue();
        }
        if(value instanceof String){
            String strVal = value.toString();
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != -1){
                strVal = strVal.replaceAll(",", "");
            }
            return Double.parseDouble(strVal);
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? 1D : 0D;
        }

        throw new JSONException("can not cast to double, value : " + value);
    }

    public static Date castToDate(Object value){
        return castToDate(value, null);
    }

    public static Date castToDate(Object value, String format){
        if(value == null){
            return null;
        }

        if(value instanceof Date){ // 使用频率最高的，应优先处理
            return (Date) value;
        }

        if(value instanceof Calendar){
            return ((Calendar) value).getTime();
        }

        long longValue = -1;

        if(value instanceof BigDecimal){
            longValue = longValue((BigDecimal) value);
            return new Date(longValue);
        }

        if(value instanceof Number){
            longValue = ((Number) value).longValue();
            if ("unixtime".equals(format)) {
                longValue *= 1000;
            }
            return new Date(longValue);
        }

        if(value instanceof String){
            String strVal = (String) value;
            JSONScanner dateLexer = new JSONScanner(strVal);
            try{
                if(dateLexer.scanISO8601DateIfMatch(false)){
                    Calendar calendar = dateLexer.getCalendar();
                    return calendar.getTime();
                }
            } finally{
                dateLexer.close();
            }

            if (strVal.startsWith("/Date(") && strVal.endsWith(")/")) {
                strVal = strVal.substring(6, strVal.length() - 2);
            }

            if (strVal.indexOf('-') > 0 || strVal.indexOf('+') > 0 || format != null) {
                if (format == null) {
                    final int len = strVal.length();
                    if (len == JSON.DEFFAULT_DATE_FORMAT.length()
                            || (len == 22 && JSON.DEFFAULT_DATE_FORMAT.equals("yyyyMMddHHmmssSSSZ"))) {
                        format = JSON.DEFFAULT_DATE_FORMAT;
                    } else if (len == 10) {
                        format = "yyyy-MM-dd";
                    } else if (len == "yyyy-MM-dd HH:mm:ss".length()) {
                        format = "yyyy-MM-dd HH:mm:ss";
                    } else if (len == 29
                            && strVal.charAt(26) == ':'
                            && strVal.charAt(28) == '0') {
                        format = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
                    } else if (len == 23 && strVal.charAt(19) == ',') {
                        format = "yyyy-MM-dd HH:mm:ss,SSS";
                    } else {
                        format = "yyyy-MM-dd HH:mm:ss.SSS";
                    }
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat(format, JSON.defaultLocale);
                dateFormat.setTimeZone(JSON.defaultTimeZone);
                try{
                    return dateFormat.parse(strVal);
                } catch(ParseException e){
                    throw new JSONException("can not cast to Date, value : " + strVal);
                }
            }
            if(strVal.length() == 0){
                return null;
            }
            longValue = Long.parseLong(strVal);
        }

        if (longValue == -1) {
            Class<?> clazz = value.getClass();
            if("oracle.sql.TIMESTAMP".equals(clazz.getName())){
                if(oracleTimestampMethod == null && !oracleTimestampMethodInited){
                    try{
                        oracleTimestampMethod = clazz.getMethod("toJdbc");
                    } catch(NoSuchMethodException e){
                        // skip
                    } finally{
                        oracleTimestampMethodInited = true;
                    }
                }
                Object result;
                try{
                    result = oracleTimestampMethod.invoke(value);
                } catch(Exception e){
                    throw new JSONException("can not cast oracle.sql.TIMESTAMP to Date", e);
                }
                return (Date) result;
            }
            if("oracle.sql.DATE".equals(clazz.getName())){
                if(oracleDateMethod == null && !oracleDateMethodInited){
                    try{
                        oracleDateMethod = clazz.getMethod("toJdbc");
                    } catch(NoSuchMethodException e){
                        // skip
                    } finally{
                        oracleDateMethodInited = true;
                    }
                }
                Object result;
                try{
                    result = oracleDateMethod.invoke(value);
                } catch(Exception e){
                    throw new JSONException("can not cast oracle.sql.DATE to Date", e);
                }
                return (Date) result;
            }

            throw new JSONException("can not cast to Date, value : " + value);
        }

        return new Date(longValue);
    }

    public static java.sql.Date castToSqlDate(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof java.sql.Date){
            return (java.sql.Date) value;
        }
        if(value instanceof Date){
            return new java.sql.Date(((Date) value).getTime());
        }
        if(value instanceof Calendar){
            return new java.sql.Date(((Calendar) value).getTimeInMillis());
        }

        long longValue = 0;
        if(value instanceof BigDecimal){
            longValue = longValue((BigDecimal) value);
        } else if(value instanceof Number){
            longValue = ((Number) value).longValue();
        }

        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(isNumber(strVal)){
                longValue = Long.parseLong(strVal);
            } else{
                JSONScanner scanner = new JSONScanner(strVal);
                if(scanner.scanISO8601DateIfMatch(false)){
                    longValue = scanner.getCalendar().getTime().getTime();
                } else{
                    throw new JSONException("can not cast to Timestamp, value : " + strVal);
                }
            }
        }
        if(longValue <= 0){
            throw new JSONException("can not cast to Date, value : " + value); // TODO 忽略 1970-01-01 之前的时间处理？
        }
        return new java.sql.Date(longValue);
    }

    public static long longExtractValue(Number number) {
        if (number instanceof BigDecimal) {
            return ((BigDecimal) number).longValueExact();
        }

        return number.longValue();
    }

    public static java.sql.Time castToSqlTime(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof java.sql.Time){
            return (java.sql.Time) value;
        }
        if(value instanceof Date){
            return new java.sql.Time(((Date) value).getTime());
        }
        if(value instanceof Calendar){
            return new java.sql.Time(((Calendar) value).getTimeInMillis());
        }

        long longValue = 0;
        if(value instanceof BigDecimal){
            longValue = longValue((BigDecimal) value);
        } else if(value instanceof Number){
            longValue = ((Number) value).longValue();
        }

        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equalsIgnoreCase(strVal)){
                return null;
            }
            if(isNumber(strVal)){
                longValue = Long.parseLong(strVal);
            } else{
                JSONScanner scanner = new JSONScanner(strVal);
                if(scanner.scanISO8601DateIfMatch(false)){
                    longValue = scanner.getCalendar().getTime().getTime();
                } else{
                    throw new JSONException("can not cast to Timestamp, value : " + strVal);
                }
            }
        }
        if(longValue <= 0){
            throw new JSONException("can not cast to Date, value : " + value); // TODO 忽略 1970-01-01 之前的时间处理？
        }
        return new java.sql.Time(longValue);
    }

    public static java.sql.Timestamp castToTimestamp(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Calendar){
            return new java.sql.Timestamp(((Calendar) value).getTimeInMillis());
        }
        if(value instanceof java.sql.Timestamp){
            return (java.sql.Timestamp) value;
        }
        if(value instanceof Date){
            return new java.sql.Timestamp(((Date) value).getTime());
        }
        long longValue = 0;
        if(value instanceof BigDecimal){
            longValue = longValue((BigDecimal) value);
        } else if(value instanceof Number){
            longValue = ((Number) value).longValue();
        }
        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.endsWith(".000000000")){
                strVal = strVal.substring(0, strVal.length() - 10);
            } else if(strVal.endsWith(".000000")){
                strVal = strVal.substring(0, strVal.length() - 7);
            }

            if (strVal.length() == 29
                    && strVal.charAt(4) == '-'
                    && strVal.charAt(7) == '-'
                    && strVal.charAt(10) == ' '
                    && strVal.charAt(13) == ':'
                    && strVal.charAt(16) == ':'
                    && strVal.charAt(19) == '.') {
                int year = num(
                        strVal.charAt(0),
                        strVal.charAt(1),
                        strVal.charAt(2),
                        strVal.charAt(3));
                int month = num(
                        strVal.charAt(5),
                        strVal.charAt(6));
                int day = num(
                        strVal.charAt(8),
                        strVal.charAt(9));
                int hour = num(
                        strVal.charAt(11),
                        strVal.charAt(12));
                int minute = num(
                        strVal.charAt(14),
                        strVal.charAt(15));
                int second = num(
                        strVal.charAt(17),
                        strVal.charAt(18));
                int nanos = num(
                        strVal.charAt(20),
                        strVal.charAt(21),
                        strVal.charAt(22),
                        strVal.charAt(23),
                        strVal.charAt(24),
                        strVal.charAt(25),
                        strVal.charAt(26),
                        strVal.charAt(27),
                        strVal.charAt(28));
                return new java.sql.Timestamp(year - 1900, month - 1, day, hour, minute, second, nanos);
            }

            if(isNumber(strVal)){
                longValue = Long.parseLong(strVal);
            } else {
                JSONScanner scanner = new JSONScanner(strVal);
                if(scanner.scanISO8601DateIfMatch(false)){
                    longValue = scanner.getCalendar().getTime().getTime();
                } else{
                    throw new JSONException("can not cast to Timestamp, value : " + strVal);
                }
            }
        }

        if(longValue < 0){
            throw new JSONException("can not cast to Timestamp, value : " + value);
        }
        return new java.sql.Timestamp(longValue);
    }

    static int num(char c0, char c1) {
        if (c0 >= '0'
                && c0 <= '9'
                && c1 >= '0'
                && c1 <= '9'
        ) {
            return (c0 - '0') * 10
                    + (c1 - '0');
        }

        return -1;
    }

    static int num(char c0, char c1, char c2, char c3) {
        if (c0 >= '0'
                && c0 <= '9'
                && c1 >= '0'
                && c1 <= '9'
                && c2 >= '0'
                && c2 <= '9'
                && c3 >= '0'
                && c3 <= '9'
        ) {
            return (c0 - '0') * 1000
                    + (c1 - '0') * 100
                    + (c2 - '0') * 10
                    + (c3 - '0');
        }

        return -1;
    }

    static int num(char c0, char c1, char c2, char c3, char c4, char c5, char c6, char c7, char c8) {
        if (c0 >= '0'
                && c0 <= '9'
                && c1 >= '0'
                && c1 <= '9'
                && c2 >= '0'
                && c2 <= '9'
                && c3 >= '0'
                && c3 <= '9'
                && c4 >= '0'
                && c4 <= '9'
                && c5 >= '0'
                && c5 <= '9'
                && c6 >= '0'
                && c6 <= '9'
                && c7 >= '0'
                && c7 <= '9'
                && c8 >= '0'
                && c8 <= '9'
        ) {
            return (c0 - '0') * 100000000
                    + (c1 - '0') * 10000000
                    + (c2 - '0') * 1000000
                    + (c3 - '0') * 100000
                    + (c4 - '0') * 10000
                    + (c5 - '0') * 1000
                    + (c6 - '0') * 100
                    + (c7 - '0') * 10
                    + (c8 - '0');
        }

        return -1;
    }

    public static boolean isNumber(String str){
        for(int i = 0; i < str.length(); ++i){
            char ch = str.charAt(i);
            if(ch == '+' || ch == '-'){
                if(i != 0){
                    return false;
                }
            } else if(ch < '0' || ch > '9'){
                return false;
            }
        }
        return true;
    }

    public static Long castToLong(Object value){
        if(value == null){
            return null;
        }

        if(value instanceof BigDecimal){
            return longValue((BigDecimal) value);
        }

        if(value instanceof Number){
            return ((Number) value).longValue();
        }

        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != -1){
                strVal = strVal.replaceAll(",", "");
            }
            try{
                return Long.parseLong(strVal);
            } catch(NumberFormatException ex){
                //
            }
            JSONScanner dateParser = new JSONScanner(strVal);
            Calendar calendar = null;
            if(dateParser.scanISO8601DateIfMatch(false)){
                calendar = dateParser.getCalendar();
            }
            dateParser.close();
            if(calendar != null){
                return calendar.getTimeInMillis();
            }
        }

        if(value instanceof Map){
            Map map = (Map) value;
            if(map.size() == 2
                    && map.containsKey("andIncrement")
                    && map.containsKey("andDecrement")){
                Iterator iter = map.values().iterator();
                iter.next();
                Object value2 = iter.next();
                return castToLong(value2);
            }
        }

        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue() ? 1L : 0L;
        }

        throw new JSONException("can not cast to long, value : " + value);
    }

    public static byte byteValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.byteValue();
        }

        return decimal.byteValueExact();
    }

    public static short shortValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.shortValue();
        }

        return decimal.shortValueExact();
    }

    public static int intValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.intValue();
        }

        return decimal.intValueExact();
    }

    public static long longValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.longValue();
        }

        return decimal.longValueExact();
    }

    public static Integer castToInt(Object value){
        if(value == null){
            return null;
        }

        if(value instanceof Integer){
            return (Integer) value;
        }

        if(value instanceof BigDecimal){
            return intValue((BigDecimal) value);
        }

        if(value instanceof Number){
            return ((Number) value).intValue();
        }

        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if(strVal.indexOf(',') != -1){
                strVal = strVal.replaceAll(",", "");
            }

            Matcher matcher = NUMBER_WITH_TRAILING_ZEROS_PATTERN.matcher(strVal);
            if(matcher.find()) {
                strVal = matcher.replaceAll("");
            }
            return Integer.parseInt(strVal);
        }

        if(value instanceof Boolean){
            return (Boolean) value ? 1 : 0;
        }
        if(value instanceof Map){
            Map map = (Map) value;
            if(map.size() == 2
                    && map.containsKey("andIncrement")
                    && map.containsKey("andDecrement")){
                Iterator iter = map.values().iterator();
                iter.next();
                Object value2 = iter.next();
                return castToInt(value2);
            }
        }
        throw new JSONException("can not cast to int, value : " + value);
    }

    public static byte[] castToBytes(Object value){
        if(value instanceof byte[]){
            return (byte[]) value;
        }
        if(value instanceof String){
            return IOUtils.decodeBase64((String) value);
        }
        throw new JSONException("can not cast to byte[], value : " + value);
    }

    public static Boolean castToBoolean(Object value){
        if(value == null){
            return null;
        }
        if(value instanceof Boolean){
            return (Boolean) value;
        }

        if(value instanceof BigDecimal){
            return intValue((BigDecimal) value) == 1;
        }

        if(value instanceof Number){
            return ((Number) value).intValue() == 1;
        }

        if(value instanceof String){
            String strVal = (String) value;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
            if("true".equalsIgnoreCase(strVal) //
                    || "1".equals(strVal)){
                return Boolean.TRUE;
            }
            if("false".equalsIgnoreCase(strVal) //
                    || "0".equals(strVal)){
                return Boolean.FALSE;
            }
            if("Y".equalsIgnoreCase(strVal) //
                    || "T".equals(strVal)){
                return Boolean.TRUE;
            }
            if("F".equalsIgnoreCase(strVal) //
                    || "N".equals(strVal)){
                return Boolean.FALSE;
            }
        }
        throw new JSONException("can not cast to boolean, value : " + value);
    }

    public static <T> T castToJavaBean(Object obj, Class<T> clazz){
        return cast(obj, clazz, ParserConfig.getGlobalInstance());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T cast(Object obj, Class<T> clazz, ParserConfig config){
        if(obj == null){
            if(clazz == int.class){
                return (T) Integer.valueOf(0);
            } else if(clazz == long.class){
                return (T) Long.valueOf(0);
            } else if(clazz == short.class){
                return (T) Short.valueOf((short) 0);
            } else if(clazz == byte.class){
                return (T) Byte.valueOf((byte) 0);
            } else if(clazz == float.class){
                return (T) Float.valueOf(0);
            } else if(clazz == double.class){
                return (T) Double.valueOf(0);
            } else if(clazz == boolean.class){
                return (T) Boolean.FALSE;
            }
            return null;
        }

        if(clazz == null){
            throw new IllegalArgumentException("clazz is null");
        }

        if(clazz == obj.getClass()){
            return (T) obj;
        }

        if(obj instanceof Map){
            if(clazz == Map.class){
                return (T) obj;
            }

            Map map = (Map) obj;
            if(clazz == Object.class && !map.containsKey(JSON.DEFAULT_TYPE_KEY)){
                return (T) obj;
            }
            return castToJavaBean((Map<String,Object>) obj, clazz, config);
        }

        if(clazz.isArray()){
            if(obj instanceof Collection){
                Collection collection = (Collection) obj;
                int index = 0;
                Object array = Array.newInstance(clazz.getComponentType(), collection.size());
                for(Object item : collection){
                    Object value = cast(item, clazz.getComponentType(), config);
                    Array.set(array, index, value);
                    index++;
                }
                return (T) array;
            }
            if(clazz == byte[].class){
                return (T) castToBytes(obj);
            }
        }

        if(clazz.isAssignableFrom(obj.getClass())){
            return (T) obj;
        }

        if(clazz == boolean.class || clazz == Boolean.class){
            return (T) castToBoolean(obj);
        }

        if(clazz == byte.class || clazz == Byte.class){
            return (T) castToByte(obj);
        }

        if(clazz == char.class || clazz == Character.class){
            return (T) castToChar(obj);
        }

        if(clazz == short.class || clazz == Short.class){
            return (T) castToShort(obj);
        }

        if(clazz == int.class || clazz == Integer.class){
            return (T) castToInt(obj);
        }

        if(clazz == long.class || clazz == Long.class){
            return (T) castToLong(obj);
        }

        if(clazz == float.class || clazz == Float.class){
            return (T) castToFloat(obj);
        }

        if(clazz == double.class || clazz == Double.class){
            return (T) castToDouble(obj);
        }

        if(clazz == String.class){
            return (T) castToString(obj);
        }

        if(clazz == BigDecimal.class){
            return (T) castToBigDecimal(obj);
        }

        if(clazz == BigInteger.class){
            return (T) castToBigInteger(obj);
        }

        if(clazz == Date.class){
            return (T) castToDate(obj);
        }

        if(clazz == java.sql.Date.class){
            return (T) castToSqlDate(obj);
        }

        if(clazz == java.sql.Time.class){
            return (T) castToSqlTime(obj);
        }

        if(clazz == java.sql.Timestamp.class){
            return (T) castToTimestamp(obj);
        }

        if(clazz.isEnum()){
            return castToEnum(obj, clazz, config);
        }

        if(Calendar.class.isAssignableFrom(clazz)){
            Date date = castToDate(obj);
            Calendar calendar;
            if(clazz == Calendar.class){
                calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
            } else{
                try{
                    calendar = (Calendar) clazz.newInstance();
                } catch(Exception e){
                    throw new JSONException("can not cast to : " + clazz.getName(), e);
                }
            }
            calendar.setTime(date);
            return (T) calendar;
        }

        String className = clazz.getName();
        if(className.equals("javax.xml.datatype.XMLGregorianCalendar")){
            Date date = castToDate(obj);
            Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
            calendar.setTime(date);
            return (T) CalendarCodec.instance.createXMLGregorianCalendar(calendar);
        }

        if(obj instanceof String){
            String strVal = (String) obj;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }

            if(clazz == Currency.class){
                return (T) Currency.getInstance(strVal);
            }

            if(clazz == Locale.class){
                return (T) toLocale(strVal);
            }

            if (className.startsWith("java.time.")) {
                String json = JSON.toJSONString(strVal);
                return JSON.parseObject(json, clazz);
            }
        }

        final ObjectDeserializer objectDeserializer = config.get(clazz);
        if (objectDeserializer != null) {
            String str = JSON.toJSONString(obj);
            return JSON.parseObject(str, clazz);
        }
        throw new JSONException("can not cast to : " + clazz.getName());
    }

    public static Locale toLocale(String strVal){
        String[] items = strVal.split("_");
        if(items.length == 1){
            return new Locale(items[0]);
        }
        if(items.length == 2){
            return new Locale(items[0], items[1]);
        }
        return new Locale(items[0], items[1], items[2]);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T castToEnum(Object obj, Class<T> clazz, ParserConfig mapping){
        try{
            if(obj instanceof String){
                String name = (String) obj;
                if(name.length() == 0){
                    return null;
                }

                if (mapping == null) {
                    mapping = ParserConfig.getGlobalInstance();
                }

                ObjectDeserializer deserializer = mapping.getDeserializer(clazz);
                if (deserializer instanceof EnumDeserializer) {
                    EnumDeserializer enumDeserializer = (EnumDeserializer) deserializer;
                    return (T) enumDeserializer.getEnumByHashCode(TypeUtils.fnv1a_64(name));
                }

                return (T) Enum.valueOf((Class<? extends Enum>) clazz, name);
            }

            if(obj instanceof BigDecimal){
                int ordinal = intValue((BigDecimal) obj);
                Object[] values = clazz.getEnumConstants();
                if(ordinal < values.length){
                    return (T) values[ordinal];
                }
            }

            if(obj instanceof Number){
                int ordinal = ((Number) obj).intValue();
                Object[] values = clazz.getEnumConstants();
                if(ordinal < values.length){
                    return (T) values[ordinal];
                }
            }
        } catch(Exception ex){
            throw new JSONException("can not cast to : " + clazz.getName(), ex);
        }
        throw new JSONException("can not cast to : " + clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Type type, ParserConfig mapping){
        if(obj == null){
            return null;
        }
        if(type instanceof Class){
            return cast(obj, (Class<T>) type, mapping);
        }
        if(type instanceof ParameterizedType){
            return (T) cast(obj, (ParameterizedType) type, mapping);
        }
        if(obj instanceof String){
            String strVal = (String) obj;
            if(strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)){
                return null;
            }
        }
        if(type instanceof TypeVariable){
            return (T) obj;
        }
        throw new JSONException("can not cast to : " + type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> T cast(Object obj, ParameterizedType type, ParserConfig mapping) {
        Type rawTye = type.getRawType();

        if(rawTye == List.class || rawTye == ArrayList.class){
            Type itemType = type.getActualTypeArguments()[0];
            if(obj instanceof List){
                List listObj = (List) obj;
                List arrayList = new ArrayList(listObj.size());

                for (int i = 0; i < listObj.size(); i++) {
                    Object item = listObj.get(i);

                    Object itemValue;
                    if (itemType instanceof Class) {
                        if (item != null && item.getClass() == JSONObject.class) {
                            itemValue = ((JSONObject) item).toJavaObject((Class<T>) itemType, mapping, 0);
                        } else {
                            itemValue = cast(item, (Class<T>) itemType, mapping);
                        }
                    } else {
                        itemValue = cast(item, itemType, mapping);
                    }

                    arrayList.add(itemValue);
                }
                return (T) arrayList;
            }
        }

        if(rawTye == Set.class || rawTye == HashSet.class //
                || rawTye == TreeSet.class //
                || rawTye == Collection.class //
                || rawTye == List.class //
                || rawTye == ArrayList.class){
            Type itemType = type.getActualTypeArguments()[0];
            if(obj instanceof Iterable){
                Collection collection;
                if(rawTye == Set.class || rawTye == HashSet.class){
                    collection = new HashSet();
                } else if(rawTye == TreeSet.class){
                    collection = new TreeSet();
                } else{
                    collection = new ArrayList();
                }
                for(Iterator it = ((Iterable) obj).iterator(); it.hasNext(); ){
                    Object item = it.next();

                    Object itemValue;
                    if (itemType instanceof Class) {
                        if (item != null && item.getClass() == JSONObject.class) {
                            itemValue = ((JSONObject) item).toJavaObject((Class<T>) itemType, mapping, 0);
                        } else {
                            itemValue = cast(item, (Class<T>) itemType, mapping);
                        }
                    } else {
                        itemValue = cast(item, itemType, mapping);
                    }

                    collection.add(itemValue);
                }
                return (T) collection;
            }
        }

        if(rawTye == Map.class || rawTye == HashMap.class){
            Type keyType = type.getActualTypeArguments()[0];
            Type valueType = type.getActualTypeArguments()[1];
            if(obj instanceof Map){
                Map map = new HashMap();
                for(Map.Entry entry : ((Map<?,?>) obj).entrySet()){
                    Object key = cast(entry.getKey(), keyType, mapping);
                    Object value = cast(entry.getValue(), valueType, mapping);
                    map.put(key, value);
                }
                return (T) map;
            }
        }
        if(obj instanceof String){
            String strVal = (String) obj;
            if(strVal.length() == 0){
                return null;
            }
        }

        Type[] actualTypeArguments = type.getActualTypeArguments();
        if (actualTypeArguments.length == 1) {
            Type argType = type.getActualTypeArguments()[0];
            if(argType instanceof WildcardType){
                return (T) cast(obj, rawTye, mapping);
            }
        }

        if (rawTye == Map.Entry.class && obj instanceof Map && ((Map) obj).size() == 1) {
            Map.Entry entry = (Map.Entry) ((Map) obj).entrySet().iterator().next();
            Object entryValue = entry.getValue();
            if (actualTypeArguments.length == 2 && entryValue instanceof Map) {
                Type valueType = actualTypeArguments[1];
                entry.setValue(
                        cast(entryValue, valueType, mapping)
                );
            }
            return (T) entry;
        }

        if (rawTye instanceof Class) {
            if (mapping == null) {
                mapping = ParserConfig.global;
            }
            ObjectDeserializer deserializer = mapping.getDeserializer(rawTye);
            if (deserializer != null) {
                String str = JSON.toJSONString(obj);
                DefaultJSONParser parser = new DefaultJSONParser(str, mapping);
                return (T) deserializer.deserialze(parser, type, null);
            }
        }

        throw new JSONException("can not cast to : " + type);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T castToJavaBean(Map<String,Object> map, Class<T> clazz, ParserConfig config){
        try{
            if(clazz == StackTraceElement.class){
                String declaringClass = (String) map.get("className");
                String methodName = (String) map.get("methodName");
                String fileName = (String) map.get("fileName");
                int lineNumber;
                {
                    Number value = (Number) map.get("lineNumber");
                    if(value == null) {
                        lineNumber = 0;
                    } else if (value instanceof BigDecimal) {
                        lineNumber = ((BigDecimal) value).intValueExact();
                    } else{
                        lineNumber = value.intValue();
                    }
                }
                return (T) new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
            }

            {
                Object iClassObject = map.get(JSON.DEFAULT_TYPE_KEY);
                if(iClassObject instanceof String){
                    String className = (String) iClassObject;
                    Class<?> loadClazz;
                    if(config == null){
                        config = ParserConfig.global;
                    }
                    loadClazz = config.checkAutoType(className, null);
                    if(loadClazz == null){
                        throw new ClassNotFoundException(className + " not found");
                    }
                    if(!loadClazz.equals(clazz)){
                        return (T) castToJavaBean(map, loadClazz, config);
                    }
                }
            }

            if(clazz.isInterface()){
                JSONObject object;
                if(map instanceof JSONObject){
                    object = (JSONObject) map;
                } else{
                    object = new JSONObject(map);
                }
                if(config == null){
                    config = ParserConfig.getGlobalInstance();
                }
                ObjectDeserializer deserializer = config.get(clazz);
                if(deserializer != null){
                    String json = JSON.toJSONString(object);
                    return JSON.parseObject(json, clazz);
                }
                return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                        new Class<?>[]{clazz}, object);
            }

            if(clazz == Locale.class){
                Object arg0 = map.get("language");
                Object arg1 = map.get("country");
                if(arg0 instanceof String){
                    String language = (String) arg0;
                    if(arg1 instanceof String){
                        String country = (String) arg1;
                        return (T) new Locale(language, country);
                    } else if(arg1 == null){
                        return (T) new Locale(language);
                    }
                }
            }

            if (clazz == String.class && map instanceof JSONObject) {
                return (T) map.toString();
            }

            if (clazz == JSON.class && map instanceof JSONObject) {
                return (T) map;
            }

            if (clazz == LinkedHashMap.class && map instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) map;
                Map innerMap = jsonObject.getInnerMap();
                if (innerMap instanceof LinkedHashMap) {
                    return (T) innerMap;
                }
            }

            if (clazz.isInstance(map)) {
                return (T) map;
            }

            if (clazz == JSONObject.class) {
                return (T) new JSONObject(map);
            }

            if (config == null) {
                config = ParserConfig.getGlobalInstance();
            }

            JavaBeanDeserializer javaBeanDeser = null;
            ObjectDeserializer deserializer = config.getDeserializer(clazz);
            if (deserializer instanceof JavaBeanDeserializer) {
                javaBeanDeser = (JavaBeanDeserializer) deserializer;
            }

            if(javaBeanDeser == null){
                throw new JSONException("can not get javaBeanDeserializer. " + clazz.getName());
            }
            return (T) javaBeanDeser.createInstance(map, config);
        } catch(Exception e){
            throw new JSONException(e.getMessage(), e);
        }
    }




    public static double parseDouble(String str) {
        final int len = str.length();
        if (len > 10) {
            return Double.parseDouble(str);
        }

        boolean negative = false;

        long longValue = 0;
        int scale = 0;
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (ch == '-' && i == 0) {
                negative = true;
                continue;
            }

            if (ch == '.') {
                if (scale != 0) {
                    return Double.parseDouble(str);
                }
                scale = len - i - 1;
                continue;
            }

            if (ch >= '0' && ch <= '9') {
                int digit = ch - '0';
                longValue = longValue * 10 + digit;
            } else {
                return Double.parseDouble(str);
            }
        }

        if (negative) {
            longValue = -longValue;
        }

        switch (scale) {
            case 0:
                return (double) longValue;
            case 1:
                return ((double) longValue) / 10;
            case 2:
                return ((double) longValue) / 100;
            case 3:
                return ((double) longValue) / 1000;
            case 4:
                return ((double) longValue) / 10000;
            case 5:
                return ((double) longValue) / 100000;
            case 6:
                return ((double) longValue) / 1000000;
            case 7:
                return ((double) longValue) / 10000000;
            case 8:
                return ((double) longValue) / 100000000;
            case 9:
                return ((double) longValue) / 1000000000;
        }

        return Double.parseDouble(str);
    }

    public static float parseFloat(String str) {
        final int len = str.length();
        if (len >= 10) {
            return Float.parseFloat(str);
        }

        boolean negative = false;

        long longValue = 0;
        int scale = 0;
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (ch == '-' && i == 0) {
                negative = true;
                continue;
            }

            if (ch == '.') {
                if (scale != 0) {
                    return Float.parseFloat(str);
                }
                scale = len - i - 1;
                continue;
            }

            if (ch >= '0' && ch <= '9') {
                int digit = ch - '0';
                longValue = longValue * 10 + digit;
            } else {
                return Float.parseFloat(str);
            }
        }

        if (negative) {
            longValue = -longValue;
        }

        switch (scale) {
            case 0:
                return (float) longValue;
            case 1:
                return ((float) longValue) / 10;
            case 2:
                return ((float) longValue) / 100;
            case 3:
                return ((float) longValue) / 1000;
            case 4:
                return ((float) longValue) / 10000;
            case 5:
                return ((float) longValue) / 100000;
            case 6:
                return ((float) longValue) / 1000000;
            case 7:
                return ((float) longValue) / 10000000;
            case 8:
                return ((float) longValue) / 100000000;
            case 9:
                return ((float) longValue) / 1000000000;
        }

        return Float.parseFloat(str);
    }






}
