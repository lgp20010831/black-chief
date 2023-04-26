package com.black.sql_v2.serialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.CalendarCodec;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.json.JsonUtils;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Av0;
import com.black.utils.ReflectionUtils;
import com.black.utils.ServiceUtils;
import lombok.ToString;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.black.utils.TypeUtils.*;

@SuppressWarnings("all")
public class SerializeUtils {

    public static List<Map<String, Object>> serializeBatch(List<Object> list){
        return null;
    }


    //@SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T deserialize(Object obj, Class<T> clazz){
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

        if(Map.class.isAssignableFrom(clazz)){
            Map<Object, Object> objectMap = ServiceUtils.createMap(clazz);
            objectMap.putAll(serialize(obj));
            return (T) objectMap;
        }
        if (Collection.class.isAssignableFrom(clazz)){
            Collection<Object> collection = ServiceUtils.createCollection(clazz);
            Collection<Object> objectCollection;
            if (obj instanceof String){
                try {
                    objectCollection = JSONArray.parseArray((String) obj);
                }catch (Throwable e){
                    objectCollection = SQLUtils.wrapList(obj);
                }

            }else {
                objectCollection = SQLUtils.wrapList(obj);
            }
            collection.addAll(objectCollection);
            return (T) collection;
        }

        if(clazz.isArray()){
            if (obj instanceof String){
                JSONArray jsonArray = JSONArray.parseArray((String) obj);
                int index = 0;
                Object array = Array.newInstance(clazz.getComponentType(), jsonArray.size());
                for(Object item : jsonArray){
                    Object value = deserialize(item, clazz.getComponentType());
                    Array.set(array, index, value);
                    index++;
                }
                return (T) array;
            }

            if(obj instanceof Collection){
                Collection collection = (Collection) obj;
                int index = 0;
                Object array = Array.newInstance(clazz.getComponentType(), collection.size());
                for(Object item : collection){
                    Object value = deserialize(item, clazz.getComponentType());
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
            return castToEnum(obj, clazz, null);
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

        if (clazz.isAssignableFrom(obj.getClass())){
            return (T) obj;
        }
        T instance = InstanceBeanManager.instance(clazz, InstanceType.REFLEX_AND_BEAN_FACTORY);
        if (instance instanceof SqlSerialize){
            return (T) ((SqlSerialize) instance).deserialize(obj.toString());
        }
        JSONObject json = JsonUtils.letJson(obj);

        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(clazz);
        Collection<FieldWrapper> fields = classWrapper.getFields();
        for (FieldWrapper field : fields) {
            String name = field.getName();
            Class<?> type = field.getType();
            Object val = json.get(name);
            Object deserialize = deserialize(val, field.getType());
            if (Collection.class.isAssignableFrom(type)){
                Class<?>[] genericVal = ReflectionUtils.genericVal(field.get(), type);
                if (genericVal.length == 1){
                    Class<?> genClazz = genericVal[0];
                    Collection<?> collection = (Collection<?>) deserialize;
                    Collection<Object> objectCollection = ServiceUtils.createCollection(type);
                    for (Object ele : collection) {
                        Object deserialized = deserialize(ele, genClazz);
                        objectCollection.add(deserialized);
                    }
                    deserialize = objectCollection;
                }
            }

            field.setValue(instance, deserialize);
        }
        return instance;
    }

    public static Map<String, Object> serialize(Object bean){
        if (bean == null){
            return new JSONObject();
        }
        if (bean instanceof Map){
            return JsonUtils.letJson(bean);
        }
        if (bean instanceof Collection){
            throw new IllegalStateException("can not serialize collection to map");
        }
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(bean);
        JSONObject json = new JSONObject(true);
        for (FieldWrapper fieldWrapper : classWrapper.getFields()) {
            Class<?> type = fieldWrapper.getType();
            String name = fieldWrapper.getName();
            Object value = fieldWrapper.getValue(bean);
            if (value == null){
                json.put(name, null);
                continue;
            }
            value = serializeValue(value);
            json.put(name, value);
        }
        return json;
    }

    public static Object serializeValue(Object value){
        if (value == null){
            return value;
        }
        if (value instanceof Map){
            return new JSONObject((Map<String, Object>) value).toJSONString();
        }

        if (value instanceof Collection){
            JSONArray array = new JSONArray();
            Collection<?> collection = (Collection<?>) value;
            for (Object ele : collection) {
                Object serializeValue = serializeValue(ele);
                array.add(serializeValue);
            }
            return array.toJSONString();
        }

        Class<?> valueClass = value.getClass();
        if (valueClass.isArray()){
            Class<?> componentType = valueClass.getComponentType();
            JSONArray jsonArray = new JSONArray();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object ele = Array.get(value, i);
                Object serializeValue = serializeValue(ele);
                jsonArray.add(serializeValue);
            }
            return jsonArray.toJSONString();
        }

        if (value instanceof SqlSerialize){
            return ((SqlSerialize) value).toSerialize();
        }

        return value;
    }

    public static void main(String[] args) {
        Map<String, Object> map = serialize(new User());
        System.out.println(map);
        User user = deserialize(map, User.class);
        System.out.println(user);
//
        User javaObject = JSON.toJavaObject(new JSONObject(map), User.class);
        System.out.println(javaObject);
    }

    @ToString
    public static class User{

        int[] array = new int[2];

        String name = "lgp";

        List<String> list = Arrays.asList("sds", "wewxd");

        List<Son> sons = Arrays.asList(new Son(), new Son());
    }

    @ToString
    public static class Son implements SqlSerialize{

        int age = 0;

        @Override
        public String toSerialize() {
            return Av0.js("age2", 2).toJSONString();
        }

        @Override
        public Object deserialize(String text) {
            System.out.println("进行反序列化");
            JSONObject jsonObject = JSONObject.parseObject(text);
            age = jsonObject.getInteger("age2");
            return this;
        }
    }
}
