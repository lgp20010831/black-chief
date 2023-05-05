package com.black.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.cache.TypeConvertCache;
import com.black.core.chain.GroupBy;
import com.black.core.convert.TypeHandler;
import com.black.map.CompareBody;
import com.black.core.builder.Sort;
import com.black.core.chain.GroupUtils;
import com.black.core.json.JsonUtils;
import com.black.core.json.ReflexUtils;
import com.black.core.json.Trust;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.*;
import com.black.scan.ChiefScanner;
import com.black.scan.ScannerManager;
import com.black.syntax.TextParseUtils;
import com.black.throwable.BreakException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("all")
public class ServiceUtils {

    public static boolean topHasAnnotation(Class<?> type, Class<? extends Annotation> annType){
        Set<Class<?>> superClasses = ClassWrapper.getSuperClasses(type);
        superClasses.add(type);
        for (Class<?> superClass : superClasses) {
            if (superClass.isAnnotationPresent(annType)){
                return true;
            }
        }
        return false;
    }

    public static boolean equalsAll(Object val, Object... objects){
        for (Object o : objects) {
            if (Objects.equals(o, val)) {
                return true;
            }
        }
        return false;
    }

    public static Object getProperty(Object bean, String fieldName){
        if (bean == null){
            return null;
        }
        if(bean instanceof Map){
            return ((Map<?, ?>) bean).get(fieldName);
        }else {
            ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(bean);
            FieldWrapper fieldWrapper = classWrapper.getField(fieldName);
            if (fieldWrapper != null){
                return fieldWrapper.getValue(bean);
            }
        }
        return null;
    }

    public static void setProperty(Object bean, String fieldName, Object value){
        if (bean == null){
            return;
        }

        if (bean instanceof Map){
            ((Map<String, Object>) bean).put(fieldName, value);
        }else {
            ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(bean);
            FieldWrapper fieldWrapper = classWrapper.getField(fieldName);
            if (fieldWrapper != null){
                fieldWrapper.setValue(bean, value);
            }
        }
    }

    public static Object getByExpression(Object ele, String expression){
        if (ele == null){
            return null;
        }
        if (!StringUtils.hasText(expression)){
            return ele;
        }
        String[] instructs = expression.contains(".") ? expression.split("\\.") : new String[]{expression};
        Object object = ele;
        for (String instruct : instructs) {
            if (object == null){
                break;
            }

            if (isBeanMethodInstruct(instruct)){
                object = parseMethod(instruct, object);
                continue;
            }

            Class<Object> primordialClass = BeanUtil.getPrimordialClass(object);
            if (Collection.class.isAssignableFrom(primordialClass) || primordialClass.isArray()){
                Integer index = com.black.utils.TypeUtils.castToInt(instruct);
                List<Object> list = SQLUtils.wrapList(object);
                object = list.get(index);
            }else if (Map.class.isAssignableFrom(primordialClass)){
                Map<String, Object> map = (Map<String, Object>) object;
                object = map.get(instruct);
            }else {
                ClassWrapper<Object> classWrapper = ClassWrapper.get(primordialClass);
                FieldWrapper fieldWrapper = classWrapper.getField(instruct);
                if (fieldWrapper != null){
                    object = fieldWrapper.getValue(object);
                }else {
                    object = null;
                }
            }
        }
        return object;
    }


    public static String patternGetValue(Object map, String expression){
        return ServiceUtils.parseTxt(expression, "${", "}", txt -> {
            return String.valueOf(ServiceUtils.getByExpression(map, txt));
        });
    }

    public static boolean isBeanMethodInstruct(String instruct){
        int of = instruct.indexOf("(");
        int ofend = instruct.indexOf(")");
        return of != -1 && ofend != -1 && ofend > of;
    }

    public static String lastName(String... array){
        if (array.length == 0){
            return null;
        }
        return array[array.length - 1];
    }

    public static String firstName(String... array){
        if (array.length == 0){
            return null;
        }
        return array[0];
    }

    public static <T> List<T> filterList(List<T> list, GroupBy<Object, T> by){
        return new ArrayList<>(GroupUtils.singleGroupArray(list, by).values());
    }


    public static <T> List<Class<T>> scanAndLoadType(String packageName, Class<T> type){
        List<Class<T>> list = new ArrayList<>();
        ChiefScanner scanner = ScannerManager.getScanner();
        Set<Class<?>> classSet = scanner.load(packageName);
        for (Class<?> clazz : classSet) {
            if (BeanUtil.isSolidClass(clazz) && type.isAssignableFrom(clazz)){
                list.add((Class<T>) clazz);
            }
        }
        return list;
    }

    public static <T> List<T> scanAndLoad(String packageName, Class<T> type){
        List<T> list = new ArrayList<>();
        ChiefScanner scanner = ScannerManager.getScanner();
        Set<Class<?>> classSet = scanner.load(packageName);
        for (Class<?> clazz : classSet) {
            if (BeanUtil.isSolidClass(clazz) && type.isAssignableFrom(clazz)){
                Object instance = InstanceBeanManager.instance(clazz, InstanceType.REFLEX_AND_BEAN_FACTORY);
                list.add((T) instance);
            }
        }
        return list;
    }

    public static <T> T getJavaEnumByName(String name, Class<T> type){
        if (!type.isEnum()) {
            throw new IllegalStateException("type is not enum");
        }
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(type);
        for (FieldWrapper field : classWrapper.getFields()) {
            if (!Modifier.isStatic(field.get().getModifiers())){
                continue;
            }
            String fn = field.getName();
            if (fn.equalsIgnoreCase(name)){
                return (T) field.getValue(null);
            }
        }
        return null;
    }

    public static <T> T mappingBean(T target, Object dataBean){
        if (dataBean != null && target != null) {
            JSONObject json = JsonUtils.letJson(dataBean);
            return mapping(target, json);
        } else {
            return target;
        }
    }

    public static <T> T mapping(T target, Map<String, Object> data){
        ClassWrapper<?> classWrapper = BeanUtil.getPrimordialClassWrapper(target);
        Collection<FieldWrapper> fields = classWrapper.getFields();
        for (FieldWrapper field : fields) {
            Class<?> type = field.getType();
            if (!SetGetUtils.hasSetMethod(field.getField())) {
                continue;
            }
            if (!data.containsKey(field.getName())){
                continue;
            }
            Object value = data.get(field.getName());
            if (value == null){
                field.setNullValue(target);
            }
            TypeHandler typeHandler = TypeConvertCache.initAndGet();
            Object convert = typeHandler.convert(type, value);
            if (Collection.class.isAssignableFrom(type)){
                Collection<Object> fieldValue = (Collection<Object>) field.getValue(target);
                fieldValue.addAll((Collection<?>) convert);
            }else if (Map.class.isAssignableFrom(type)){
                Map<Object, Object> fieldValue = (Map<Object, Object>) field.getValue(target);
                fieldValue.putAll((Map<?, ?>) convert);
            }else {
                SetGetUtils.invokeSetMethod(field.getField(), convert, target);
            }
        }
        return target;
    }

    //根据key取出每个元素然后根据连接符连接
    public static String appendItem(List<Map<String, Object>> source, String key, String interval){
        StringJoiner joiner = new StringJoiner(interval);
        for (Map<String, Object> map : source) {
            String value = ServiceUtils.getString(map, key);
            joiner.add(value);
        }
        return joiner.toString();
    }
    public static List<Map<String, Object>> groupByMap(List<Map<String, Object>> list,
                                                       String listKey,
                                                       String... keys){
        return groupByMap(list, listKey, true, keys);
    }


    //把指定keys 当做key分组
    public static List<Map<String, Object>> groupByMap(List<Map<String, Object>> list,
                                                       String listKey,
                                                       boolean remove,
                                                       String... keys){
        Map<CompareBody, List<Map<String, Object>>> array = GroupUtils.groupArray(list, map -> {
            CompareBody compareKeyMap = new CompareBody();
            for (String key : keys) {
                compareKeyMap.put(key, remove ? map.remove(key) : map.get(key));
            }
            return compareKeyMap;
        });
        List<Map<String, Object>> result = new ArrayList<>();
        for (CompareBody map : array.keySet()) {
            List<Map<String, Object>> mapList = array.get(map);
            map.put(listKey, mapList);
            result.add(map);
        }
        return result;
    }

    public static <K,V> Map<K, V> filterNullValueMap(Map<K, V> map){
        if (map == null){
            return map;
        }
        map.keySet().removeIf(k -> {
            return map.get(k) == null;
        });
        return map;
    }

    public static <T> T findInArray(Object[] array, Class<T> type){
        for (Object ele : array) {
            if (ele != null){
                Class<Object> primordialClass = BeanUtil.getPrimordialClass(ele);
                if (type.isAssignableFrom(primordialClass)){
                    return (T) ele;
                }
            }
        }
        return null;
    }

    public static <K,V> Map<K, V> filterNewMap(Map<K, V> param, List<K> names){
        Map<K, V> newMap = new LinkedHashMap<>();
        for (K name : names) {
            V v = param.get(name);
            newMap.put(name, v);
        }
        return newMap;
    }

    public static <T> T[] addArray(T[] source, T ele){
        return addArray(source, ele, false);
    }

    public static <T> T[] addArray(T[] source, T ele, boolean first){
        LinkedList<T> list = new LinkedList<>(Arrays.asList(source));
        if (first){
            list.addFirst(ele);
        }else {
            list.addLast(ele);
        }
        return (T[]) list.toArray();
    }

    public static <K, V> boolean containKeys(Map<K, V> map, K... keys){
        if (map == null){
            return false;
        }
        for (K key : keys) {
            if (!map.containsKey(key)){
                return false;
            }
        }
        return true;
    }
    public static <T> T arrayIndex(T[] array, int index){
        if (array.length < index + 1){
            throw new IllegalStateException("array size Not meeting expectations");
        }
        return array[index];
    }

    public static String getBinaryStrFromByte(byte b){
        String r ="";
        byte a = b; ;
        for (int i = 0; i < 8; i++){
            byte c=a;
            a=(byte)(a>>1);//每移一位如同将10进制数除以2并去掉余数。
            a=(byte)(a<<1);
            if(a==c){
                r="0" + r;
            }else{
                r="1" + r;
            }
            a=(byte)(a>>1);
        }
        return r;
    }

    public static String toPointHexString(int i, int size){
        String hexString = Integer.toHexString(i);
        if (hexString.length() > size){
            throw new IllegalStateException("hex size is not " + size + "; hex:" + hexString);
        }
        if (hexString.length() < size){
            int length = hexString.length();
            for (int i1 = 0; i1 < size - length; i1++) {
                hexString = "0" + hexString;
            }
        }
        return hexString;
    }

    @SafeVarargs
    public static <T> Collection<T> mergePip(T... objs){
        Object collection = merge((Object[]) objs);
        return (Collection<T>) collection;
    }

    public static Collection<Object> merge(Object... objs){
        ArrayList<Object> list = new ArrayList<>();
        for (Object obj : objs) {
            if (obj instanceof Collection){
                ((Collection<?>) obj).forEach(e -> addCollection(list, e));
            }else {
                addCollection(list, obj);
            }
        }
        return list;
    }

    public static <K, V> Map<K, V> putMap(Map<K, V> map, K k, V v){
        if (map != null && k != null){
            map.put(k, v);
        }
        return map;
    }

    public static <T> Collection<T> addCollection(Collection<T> collection, T obj){
        if (collection != null && obj != null && !collection.contains(obj)){
            collection.add(obj);
        }
        return collection;
    }

    public static int getSize(Object bean){
        return getSize(bean, true);
    }

    public static int getSize(Object bean, boolean allowArrayOrMap){
        if (bean == null)
            return 0;
        if (bean instanceof Collection){
            return ((Collection<?>) bean).size();
        }

        if (bean instanceof Map){
            return ((Map<?, ?>) bean).size();
        }

        if (bean.getClass().isArray()){
            return ((Object[])bean).length;
        }

        if (allowArrayOrMap){
            throw new IllegalStateException("bean is not a array or map");
        }

        return bean.toString().length();
    }

    public static <K, V> Map<K, V> createMap(Class<?> mapType){
        if (Map.class.isAssignableFrom(mapType)){
            if (BeanUtil.isSolidClass(mapType)){
                return (Map<K, V>) ReflexUtils.instance(mapType);
            }
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>();
    }

    public static <T> Collection<T> createCollection(Class<?> returnType){
        if (Collection.class.isAssignableFrom(returnType)){
            if (Set.class.isAssignableFrom(returnType)){
                if (!BeanUtil.isSolidClass(returnType)){
                    return new HashSet<>();
                }
            }
            if (List.class.isAssignableFrom(returnType)){
                if (!BeanUtil.isSolidClass(returnType)){
                    return new ArrayList<>();
                }
            }

            if (Queue.class.isAssignableFrom(returnType)){
                if (!BeanUtil.isSolidClass(returnType)){
                    return new LinkedBlockingQueue<>();
                }
            }

            if (BeanUtil.isSolidClass(returnType)){
                return (Collection<T>) ReflexUtils.instance(returnType);
            }
        }
        return new ArrayList<>();
    }

    public static InputStream getNonNullResource(String path){
        InputStream resource = getResource(path);
        Assert.notNull(resource, "not find resource:" + path);
        return resource;
    }

    public static InputStream getResource(String path){
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    public static boolean existResource(String path){
        return Thread.currentThread().getContextClassLoader().getResource(path) != null;
    }

    public static Map<String, Object> castMap(Map<String, Object> map){
        Map<String, Object> result = new LinkedHashMap<>();
        if (map == null) return result;
        result.putAll(map);
        return result;
    }

    public static <K, V> Map<K, V> copyMap(Map<K, V> old){
        if (old == null){
            return new LinkedHashMap<>();
        }
        Map<K, V> map = new LinkedHashMap<>(old);
        return map;
    }

    public static boolean containThrowableMessage(Throwable e, String str){
        Throwable cause = e;
        while (cause != null){
            String message = cause.getMessage();
            if (message != null && message.contains(str)){
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    public static String getThrowableMessage(Throwable e){
        return getThrowableMessage(e, "unknown");
    }

    public static String getThrowableMessage(Throwable e, String defaultMsg){
        if (e == null){
            return defaultMsg;
        }
        String msg = null;
        Throwable cause = e;
        while (cause != null){
            String message = cause.getMessage();
            if (StringUtils.hasText(message)){
                msg = message;
                break;
            }
            cause = cause.getCause();
        }
        return StringUtils.hasText(msg) ? msg : defaultMsg;
    }

    public static <K, V> V replaceKey(Map<K, V> map, K old, K newK){
        if (map.containsKey(old)){
            V v = map.remove(old);
            map.put(newK, v);
            return v;
        }
        return null;
    }

    public static <E> List<E> sortTree(List<E> list,
                                       Function<E, Object> function,
                                       String childrenFieldName,
                                       boolean asc){
        if (list == null) return list;
        sort(list, function, asc);
        try {
            for (E e : list) {
                if (e == null) continue;
                Class<?> type = e.getClass();
                Field df = type.getDeclaredField(childrenFieldName);
                df.setAccessible(true);
                List<E> objList = (List<E>) df.get(e);
                if (objList != null){
                    sort(objList, function, asc);
                    sortTree(objList, function, childrenFieldName, asc);
                }
            }
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }
        return list;
    }

    public static Comparator<Object> comparableDesc = (v1, v2) -> {

        if (v1 instanceof Number && v2 instanceof Number){
            Number n1 = (Number) v1;
            Number n2 = (Number) v2;
            return ((Double)(n2.doubleValue() - n1.doubleValue())).intValue();
        }
        if (Objects.equals(v1, v2)) {
            return 0;
        }

        if (v1 instanceof Comparator<?> && v2 instanceof Comparator<?>){
            return((Comparator<Object>) v1).compare(v2, v1);
        }
        String s1 = v1 == null ? "null" : v1.toString();
        String s2 = v2 == null ? "null" : v2.toString();
        int i = s1.compareTo(s2);
        return i;
    };


    public static Comparator<Object> comparableAsc = (v1, v2) -> {

        if (v1 instanceof Number && v2 instanceof Number){
            Number n1 = (Number) v1;
            Number n2 = (Number) v2;
            return ((Double)(n1.doubleValue() - n2.doubleValue())).intValue();
        }
        if (Objects.equals(v1, v2)) {
            return 0;
        }

        if (v1 instanceof Comparator<?> && v2 instanceof Comparator<?>){
            return((Comparator<Object>) v1).compare(v1, v2);
        }
        String s1 = v1 == null ? "null" : v1.toString();
        String s2 = v2 == null ? "null" : v2.toString();
        int i = s2.compareTo(s1);
        return i;
    };

    public static <E> List<E> sortInt(List<E> source, Function<E, Object> function, boolean asc, int def){
        source.sort((o1, o2) -> {
            Object v1 = function.apply(o1);
            Object v2 = function.apply(o2);
            if (v1 instanceof Number && v2 instanceof Number){
                Number n1 = (Number) v1;
                Number n2 = (Number) v2;
                return ((Double)(asc ? n1.doubleValue() - n2.doubleValue() : n2.doubleValue() - n1.doubleValue())).intValue();
            }
            if (Objects.equals(v1, v2)) {
                return 0;
            }

            int s1 = v1 == null ? def : Integer.parseInt(v1.toString());
            int s2 = v2 == null ? def : Integer.parseInt(v2.toString());
            int i = asc ? s1 - s2 : s2 - s1;
            return i;
        });
        return source;
    }


    public static <E> List<E> sort(List<E> source, Function<E, Object> function, boolean asc){
        source.sort((o1, o2) -> {
            Object v1 = function.apply(o1);
            Object v2 = function.apply(o2);
            if (v1 instanceof Number && v2 instanceof Number){
                Number n1 = (Number) v1;
                Number n2 = (Number) v2;
                return ((Double)(asc ? n1.doubleValue() - n2.doubleValue() : n2.doubleValue() - n1.doubleValue())).intValue();
            }
            if (Objects.equals(v1, v2)) {
                return 0;
            }

            if (v1 instanceof Comparator<?> && v2 instanceof Comparator<?>){
                return asc ? ((Comparator<Object>) v1).compare(v1, v2) : ((Comparator<Object>) v1).compare(v2, v1);
            }
            String s1 = v1 == null ? "null" : v1.toString();
            String s2 = v2 == null ? "null" : v2.toString();
            int i = asc ? s1.compareTo(s2) : s2.compareTo(s1);
            return i;
        });
        return source;
    }

    public static <K, V> Map<K, V> ofMap(K k, V v){
        return Vfu.of(k, v);
    }

    public static <K, V> Map<K, V> ofMap(K k, V v, K k2, V v2){
        return Vfu.of(k, v, k2, v2);
    }

    public static JSONObject ofJson(String k, Object v){
        return Vfu.js(k, v);
    }

    public static JSONObject ofJson(String k, Object v, String k2, Object v2){
        return Vfu.js(k, v, k2, v2);
    }

    public static Object ill(boolean result, String msg){
        return ill(result, msg, null);
    }

    public static Object ill(boolean result, String msg, Object defaultValue){
        if (result){
            throw new IllegalStateException(msg);
        }
        return defaultValue;
    }

    public static List<Double> getNonDoubleList(Map<String, Object> map, String key, Double defaultValue){
        List<Double> list = getDoubleList(map, key, defaultValue);
        return list == null ? new ArrayList<>() : list;
    }

    public static List<Double> getDoubleList(Map<String, Object> map, String key, Double defaultValue){
        Object val = map.get(key);
        if (val == null){
            return null;
        }
        List<Object> list = SQLUtils.wrapList(val);
        return StreamUtils.mapList(list, ele -> {
            return ele == null ? defaultValue : Double.parseDouble(ele.toString());
        });
    }

    public static Map<String, Object> getMap(Map<String, Object> map, String key){
        return JsonUtils.letJson(map.get(key));
    }

    public static List<Integer> getNonIntList(Map<String, Object> map, String key, Integer defaultValue){
        List<Integer> list = getIntList(map, key, defaultValue);
        return list == null ? new ArrayList<>() : list;
    }

    public static List<Integer> getIntList(Map<String, Object> map, String key, Integer defaultValue){
        Object val = map.get(key);
        if (val == null){
            return null;
        }
        List<Object> list = SQLUtils.wrapList(val);
        return StreamUtils.mapList(list, ele -> {
            return ele == null ? defaultValue : Integer.parseInt(ele.toString());
        });
    }


    public static List<String> getNonStringList(Map<String, Object> map, String key){
        List<String> list = getStringList(map, key);
        return list == null ? new ArrayList<>() : list;
    }

    public static List<String> getStringList(Map<String, Object> map, String key){
        Object val = map.get(key);
        if (val == null){
            return null;
        }
        List<Object> list = SQLUtils.wrapList(val);
        return StreamUtils.mapList(list, ele -> {
            return ele == null ? null : ele.toString();
        });
    }

    public static List<Map<String, Object>> getNonList(Map<String, Object> map, String key){
        List<Map<String, Object>> list = getList(map, key);
        return list == null ? new ArrayList<>() : list;
    }

    public static List<Map<String, Object>> getList(Map<String, Object> map, String key){
        Object val = map.get(key);
        if (val == null){
            return null;
        }
        List<Object> list = SQLUtils.wrapList(val);
        return StreamUtils.mapList(list, JsonUtils::letJson);
    }

    public static String staticString(String txt, String defaultTxt){
        if (!com.black.core.util.StringUtils.hasText(txt)){
            return defaultTxt;
        }
        return txt;
    }

    public static String getDoubleString(double d, int scal){
        return String.format("%." + scal + "f", d);
    }


    public static <K> K findSingle(List<K> list, String name, Object value){
        for (K k : list) {
            Map<String, Object> map = (Map<String, Object>) JSON.toJSON(k);
            Object val = map.get(name);
            if (value.equals(val)){
                return k;
            }
        }
        return null;
    }

    public static <K, V, U> Map<K, U> replaceMapValue(Map<K, V> map, Function<V, U> function, U defaultValue){
        Map<K, U> result = new HashMap<>();
        for (K k : map.keySet()) {
            V v = map.get(k);
            if (v == null){
                result.put(k, defaultValue);
            }else {
                U apply = function.apply(v);
                result.put(k, apply == null ? defaultValue : apply);
            }
        }
        return result;
    }

    public static String castString(Object obj){
        if (obj instanceof String){
            return (String) obj;
        }
        if (obj instanceof byte[]){
            return new String((byte[]) obj);
        }

        if (obj instanceof char[]){
            return new String((char[]) obj);
        }
        if (obj instanceof Collection){
            JSONArray array = new JSONArray();
            Collection<?> collection = (Collection<?>) obj;
            array.addAll(collection);
            return array.toString();
        }

        if (obj instanceof Map){
            return new JSONObject((Map<String, Object>) obj).toString();
        }
        return obj == null ? null : obj.toString();
    }

    public static <U, K, V> U getValue(Map<K, V> map, K k, U defaultValue, String... loop){
        V v = map.get(k);
        if (v == null){
            return defaultValue;
        }
        Object target = v;
        for (String name : loop) {
            if (target == null){
                return defaultValue;
            }
            Object o = JSON.toJSON(target);
            if (o instanceof Map){
                Map<String, Object> m = (Map<String, Object>) o;
                target = m.get(name);
                continue;
            }
            return defaultValue;
        }
        return (U) target;
    }

    public static <K> Double sumD(List<K> list, String name){
        Double r = 0d;
        for (K k : list) {
            Map<String, Object> map = (Map<String, Object>) JSON.toJSON(k);
            Object val = map.get(name);
            if (val != null){
                r += Double.parseDouble(val.toString());
            }
        }
        return r;
    }


    public static <K> Integer sum(List<K> list, String name){
        Integer r = 0;
        for (K k : list) {
            Map<String, Object> map = (Map<String, Object>) JSON.toJSON(k);
            Object val = map.get(name);
            if (val != null){
                r += Integer.parseInt(val.toString());
            }
        }
        return r;
    }

    public static Object find(Object source, String key){
        if(source == null || key == null) return null;
        if(source instanceof Map){
            Map<String, Object> map = (Map<String, Object>) source;
            return map.get(key);
        }

        Class<Object> primordialClass = BeanUtil.getPrimordialClass(source);
        ClassWrapper<Object> cw = ClassWrapper.get(primordialClass);
        FieldWrapper fw = cw.getField(key);
        if (fw != null){
            return SetGetUtils.invokeGetMethod(fw.getField(), source);
        }
        return null;
    }

    //****************************************************
    //                      时间操作
    //****************************************************
    //返回毫秒值
    public static long getInitialDelay(int hour, int min){
        Date now = new Date();
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, hour);
        instance.set(Calendar.MINUTE, min);
        Date target = instance.getTime();

        if (target.before(now)){
            target = addDay(target, 1);
        }
        long l = target.getTime() - now.getTime();
        return l;
    }

    public static Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }


    public static String supplement(Object text, int size, boolean front, char c){
        String textStr = text == null ? "" : text.toString();
        if (textStr.length() >= size) return textStr;
        StringBuilder textBuilder = new StringBuilder(textStr);
        for (int i = 0; i < size - textBuilder.length(); i++) {
            if (front){
                textBuilder.insert(0, c);
            }else {
                textBuilder.append(c);
            }
        }
        return textBuilder.toString();
    }

    public interface ScannerHandler{

        void handle(String in) throws BreakException;
    }

    public static void systemScanIn(@NonNull ScannerHandler handler){
        Scanner scanner = new Scanner(System.in);
        for (;;){
            String next = scanner.next();
            try {
                handler.handle(next);
            }catch (BreakException e){
                break;
            }
        }
    }

    public static String v2Now(){
        Calendar instance = Calendar.getInstance();
        int year = instance.get(Calendar.YEAR);
        int month = instance.get(Calendar.MONTH) + 1;
        int day = instance.get(Calendar.DATE);
        int hour = instance.get(Calendar.HOUR_OF_DAY);
        int min = instance.get(Calendar.MINUTE);
        int sec = instance.get(Calendar.SECOND);
        int msec = instance.get(Calendar.MILLISECOND);
        String monthStr = supplement(month, 2, true, '0');
        String dayStr = supplement(day, 2, true, '0');
        String hourStr = supplement(hour, 2, true, '0');
        String minStr = supplement(min, 2, true, '0');
        String secStr = supplement(sec, 2, true, '0');
        String msecStr = supplement(msec, 3, true, '0');
        return com.black.core.util.StringUtils.letString(year, "-",
                monthStr, "-", dayStr, " ", hourStr, ":", minStr
                , ":", secStr, ".", msecStr);
    }

    public static String now(){
        return now("yyyy-MM-dd HH:mm:ss");
    }

    public static String now(String format){
        return new SimpleDateFormat(format).format(new Date());
    }

    public static Date addTime(Date date, Integer day){
        if (date == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, day == null ? 5 : day);
        return calendar.getTime();
    }

    public static Date parseDate(String format, String value){
        try {
            return new SimpleDateFormat(format).parse(value);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Calendar parseCalendar(String format, String value){
        Date date = parseDate(format, value);
        return getCalendar(date);
    }

    public static Calendar getCalendar(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    //****************************************************
    //      方便实现一对多查询, 对结果进行收集并整合
    //****************************************************

    /***
     * 源数据 A
     * 子表 B
     * 子表 C
     * return integration(A结果集, mapFun("id"), create(B结果集, "bList", mapFun(aId)), create(C结果集, "cList", mapFun(aId)))
     * @param source 最终结果
     * @param fun 获取源数据匹配的函数
     * @param groups 每个 group 等于封装了一类子查询
     * @return 返回将所有 group 整合到源数据的值
     */
    public static List<Map<String, Object>> integration(List<Map<String, Object>> source, @NonNull FUN<?> fun, FunGroup... groups){
        if (source == null){
            return new ArrayList<>();
        }
        if (groups == null || groups.length == 0){
            return source;
        }

        for (Map<String, Object> map : source) {
            Object mapping = fun.getMapping(map);
            if (mapping != null){
                for (FunGroup group : groups) {
                    @SuppressWarnings("all")
                    List<Map<String, Object>> array = (List<Map<String, Object>>) map.computeIfAbsent(group.name, na -> new ArrayList<>());
                    List<Map<String, Object>> list = group.map.get(mapping);
                    if (list != null){
                        array.addAll(list);
                    }
                }
            }
        }
        return source;
    }

    //简化构造 Fun 函数
    @SuppressWarnings("all")
    public static <T> FUN<T> mapfun(String key){
        return new FUN<T>() {
            @Override
            public T getMapping(Map<String, Object> map) {
                return (T) map.get(key);
            }
        };
    }

    @SuppressWarnings("all")
    public static FUN<String> strfun(String key){
        return new FUN<String>() {
            @Override
            public String getMapping(Map<String, Object> map) {
                return getString(map, key);
            }
        };
    }

    //构造 funGroup
    public static FunGroup create(List<Map<String, Object>> subSource, String name, FUN<?> fun){
        return new FunGroup(name, fun, subSource);
    }

    @Getter
    public static class FunGroup{
        private final String name;
        private final FUN<?> fun;
        private final Map<Object, List<Map<String, Object>>> map = new HashMap<>();

        public FunGroup(String name, FUN<?> fun, List<Map<String, Object>> subSource) {
            this.name = name;
            this.fun = fun;
            if (subSource != null && !subSource.isEmpty()){
                map.putAll(GroupUtils.groupList(subSource, fun::getMapping));
            }
        }
    }

    public interface FUN<R>{
        R getMapping(Map<String, Object> map);
    }

    //****************************************************
    //                      排序操作
    //****************************************************
    public interface GetSort<E>{
        int getSort(E element);
    }

    /***
     * 排序一个集合
     * @param maps 数据源
     * @param getSort 获取排序的字段, 一个函数, 传值为数据源的每个元素
     * @param <E> 数据源类型
     * @return 数据源
     */
    @SuppressWarnings("ALL")
    public static <E> List<E> sortList(List<E> maps, GetSort<E> getSort){
        Collections.sort(maps, (map1, map2) ->{
            return getSort.getSort(map1) - getSort.getSort(map2);
        });
        return maps;
    }



    //****************************************************
    //                      map 规整操作
    //****************************************************

    public static <K, V> boolean isEmpty(Map<K, V> map){
        return map == null || map.isEmpty();
    }


    public static <V> Integer loopFind(Map<String, V> map, String target, Integer defaultValue){
        if (map == null){
            return defaultValue;
        }
        if (map.containsKey(target)) {
            return getInt((Map<String, Object>) map, target);
        }
        for (String key : map.keySet()) {
            V v = map.get(key);
            if (v instanceof Map){
                Integer find = loopFind((Map<String, V>) v, target, (Integer) null);
                if (find != null){
                    return find;
                }
            }
        }
        return defaultValue;
    }

    public static <V> Boolean loopFind(Map<String, V> map, String target, Boolean defaultValue){
        if (map == null){
            return defaultValue;
        }
        if (map.containsKey(target)) {
            return getBoolean((Map<String, Object>) map, target, defaultValue);
        }
        for (String key : map.keySet()) {
            V v = map.get(key);
            if (v instanceof Map){
                Boolean find = loopFind((Map<String, V>) v, target, (Boolean) null);
                if (find != null){
                    return find;
                }
            }
        }
        return defaultValue;
    }

    public static <V> String loopFind(Map<String, V> map, String target, String defaultValue){
        if (map == null){
            return defaultValue;
        }
        if (map.containsKey(target)) {
            return getString((Map<String, Object>) map, target);
        }
        for (String key : map.keySet()) {
            V v = map.get(key);
            if (v instanceof Map){
                String find = loopFind((Map<String, V>) v, target, (String) null);
                if (find != null){
                    return find;
                }
            }
        }
        return defaultValue;
    }

    public static <V> String loopGet(Map<String, V> map, String... keys){
        if (map == null){return null;}
        String result;
        Map<String, V> pool = map;
        Object value = null;
        for (String key : keys) {
            if (!pool.containsKey(key)){
                break;
            }
            value = pool.get(key);
            if (value instanceof Map){
                pool = (Map<String, V>) value;
                continue;
            }
            break;
        }
        if (!(value instanceof String)){
            result = value == null ? null : value.toString();
        }else {
            result = (String) value;
        }
        return result;
    }

    public static List<Map<String, Object>> turn(List<Map<String, Object>> maps,
                                                 String key){
        return turn(maps, key, null);
    }

    public static List<Map<String, Object>> turn(List<Map<String, Object>> maps,
                                                            String key, Consumer<? super Map<String, Object>> action){
        for (Map<String, Object> map : maps) {
            Object v = map.get(key);
            if (v != null){
                JSONArray array = JSON.parseArray(v.toString());
                for (Object ele : array) {
                    if (ele != null){
                        JSONObject object = JSON.parseObject(ele.toString());
                        map.putAll(object);
                    }
                }
            }
            if (action != null)
                action.accept(map);
        }
        return maps;
    }

    /***
     * map = {铁=12,铜=48,银=18,金=2....}
     * -> turn(map, "type", "count")
     * -> [{type=铁,count=12}, {type=铜,count=48}, {type=银,count=18}, {type=金,count=2}.....]
     * @param map 目标数据
     * @param keyKey 将 key 额外封装的新 key
     * @param valKey 将 val 额外封装的新 key
     * @param <K> 类型
     * @param <V> 类型
     * @return 将 map 分组后数据源
     */
    public static <K, V> List<Map<String, ?>> turn(Map<K, V> map, String keyKey, String valKey){
        if (map == null){
            return new ArrayList<>();
        }
        List<Map<String, ?>> result = new ArrayList<>();
        map.forEach((k, v) ->{
            result.add(Av0.of(keyKey, k, valKey, v));
        });
        return result;
    }


    public static Date getDate(Map<String, Object> map, String key){
        return TypeUtils.castToDate(map.get(key));
    }


    //get int in map
    public static Integer getInt(Map<String, Object> map, String key){
        if (map == null){
            return null;
        }
        Object val = map.get(key);
        return val == null ? 0 : Integer.parseInt(val.toString());
    }

    public static double getDouble(Map<String, Object> map, String key){
        if (map == null){
            return 0;
        }
        Object val = map.get(key);
        return val == null ? 0 : Double.parseDouble(val.toString());
    }

    public static String createUUID(){
        return UUID.randomUUID().toString();
    }

    public static <T> List<T> sortCollection(Collection<T> collection, Function<T, Object> function, boolean asc){
        ArrayList<T> list = new ArrayList<>(collection);
        return sort(list, function, asc);
    }

    public static <T> List<T> sort(List<T> source, List<Sort> sortInfos){
        source.sort((o1, o2) -> {
            for (Sort sortInfo : sortInfos) {
                String sortName = sortInfo.getSortName();
                Object v1 = ServiceUtils.getSortValue(o1, sortName);
                Object v2 = ServiceUtils.getSortValue(o2, sortName);
                if (v1 instanceof Number && v2 instanceof Number){
                    Number n1 = (Number) v1;
                    Number n2 = (Number) v2;
                    return ((Double)(sortInfo.isAsc() ? n1.doubleValue() - n2.doubleValue() : n2.doubleValue() - n1.doubleValue())).intValue();
                }
                if (v2.equals(v1)) continue;
                String s1 = v1.toString();
                String s2 = v2.toString();
                int i = sortInfo.isAsc() ? s1.compareTo(s2) : s2.compareTo(s1);
                return i;
            }
            return 0;
        });
        return source;
    }

    public static List<Map<String, Object>> sort(List<Map<String, Object>> source, String sortName, int nullSortDefaultValue, boolean asc){
        source.sort((o1, o2) -> {
            int v1 = ServiceUtils.getSortValue(o1, sortName, nullSortDefaultValue);
            int v2 = ServiceUtils.getSortValue(o2, sortName, nullSortDefaultValue);
            return asc ? v1 - v2 : v2 - v1;
        });
        return source;
    }

    public static Object findValue(Object argMap, String entry){
        if (entry == null) return null;
        Object val = argMap;
        for (String e : entry.split("\\.")) {
            if (val == null) return null;
            if (val instanceof Map){
                Map<String, Object> map = (Map<String, Object>) val;
                val = map.get(e);
            }else {
                val = SetGetUtils.invokeGetMethod(e, val);
            }
        }
        return val;
    }

    public static Object findVal(Object argMap, String entry){
        if (entry == null) return null;
        Object val = argMap;
        for (String e : entry.split("\\.")) {
            if (val == null) return null;
            if (val instanceof Map){
                Map<String, Object> map = (Map<String, Object>) val;
                val = map.get(e);
            }else {
                val = SetGetUtils.invokeGetMethod(e, val);
            }
        }
        return val;
    }

    public static Object getSortValue(Object source, String sortName){
        if (sortName.contains(".")) {
            Object mapVal = source;
            for (String n : sortName.split("\\.")) {
                mapVal = ServiceUtils.getValue(mapVal, n);
                if (mapVal == null){
                    return "null";
                }
            }
            return mapVal;
        }else {
            Object value = getValue(source, sortName);
            return value == null ? "null" : value;
        }
    }

    public static int getSortValue(Map<String, Object> source, String sortName, int nullSortDefaultValue){
        int v = nullSortDefaultValue;
        if (sortName.contains(".")) {
            Object mapVal = source;
            for (String n : sortName.split("\\.")) {
                mapVal = ServiceUtils.getValue(mapVal, n);
                if (mapVal == null){
                    return v;
                }
            }
            v = Integer.parseInt(mapVal.toString());
        }else {
            Object intval = source.get(sortName);
            v = intval == null ? nullSortDefaultValue : Integer.parseInt(intval.toString());
        }
        return v;
    }

    public static <E> Map<String, Object> buildMapping(E source){
        Map<String, Object> map;
        if (source instanceof Map){
            map = (Map<String, Object>) source;
        }else {
            map = JsonUtils.letJson(source);
        }
        return map;
    }

    public static Object getValue(Object bean, String key){
        if (bean == null || key == null)
            return null;
        if (bean instanceof Map){
            return ((Map<String, Object>) bean).get(key);
        }else {
            return SetGetUtils.invokeGetMethod(key, bean);
        }
    }

    public static String getString(Object obj, String dv){
        if (obj == null){
            return dv;
        }
        return obj.toString();
    }

    public static String getString(Object obj){
        if (obj == null){
            return "null";
        }
        return obj.toString();
    }

    public static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue){
        if (map == null){
            return defaultValue;
        }
        Object val = map.get(key);
        return val == null ? defaultValue : Boolean.parseBoolean(val.toString());
    }

    //get str in map
    public static String getString(Map<String, Object> map, String key){
        if (map == null){
            return null;
        }
        Object val = map.get(key);
        return val == null ? null : val.toString();
    }

    public static Map<String, Object> groupMerge(Map<String, Object> source,
                                                 List<Map<String, Object>> target,
                                                 FUN<String> fun,
                                                 String echoName){
        return groupMerge(source, target, fun, fun, echoName);
    }

    public static Map<String, Object> groupMerge(Map<String, Object> source,
                                                       List<Map<String, Object>> target,
                                                       FUN<String> fun,
                                                       FUN<String> tarfun,
                                                       String echoName){
        List<Map<String, Object>> sourcelist = Collections.singletonList(source);
        groupMerge(sourcelist, target, fun, tarfun, echoName);
        return sourcelist.get(0);
    }


    //将 target 合并到 source 中
    public static List<Map<String, Object>> groupMerge(List<Map<String, Object>> source,
                                  List<Map<String, Object>> target,
                                  FUN<String> fun,
                                  String echoName){
        return groupMerge(source, target, fun, fun, echoName);
    }

    /****
     * yuan = [{id=3,name=lsp....}....]
     * mu = [{tar=3,agr=15...}.....]
     * -> groupMerge(yuan, mu, strfun(id), strfun(tar), "mulist")
     * -> [{id=3,name=lsp....,mulist:[{tar=3,agr=15...}...]}....]
     * 效果差不多等同于  integration
     * @param source  源数据
     * @param target 目标数据
     * @param sourceMapping 对数据源 key 的映射函数
     * @param targetMapping 对目标数据 key 的映射函数
     * @param echoName 加入到源数据的 key name
     */
    public static List<Map<String, Object>> groupMerge(List<Map<String, Object>> source,
                                  List<Map<String, Object>> target,
                                  FUN<String> sourceMapping,
                                  FUN<String> targetMapping,
                                  String echoName){
        //将 source 根据 mapping 进行分组
        Map<String, Map<String, Object>> mapping = new HashMap<>();
        for (Map<String, Object> map : source) {
            String key = sourceMapping.getMapping(map);
            mapping.put(key, map);
        }

        //
        for (Map<String, Object> map : target) {
            String key = targetMapping.getMapping(map);
            Map<String, Object> sourceMap = mapping.get(key);
            if (sourceMap != null){

                @SuppressWarnings("all")
                List<Object> list = (List<Object>) sourceMap.computeIfAbsent(echoName, en -> new ArrayList<>());
                list.add(map);
            }
        }
        return source;
    }


    public static List<String> listGet(List<Map<String, Object>> maps, FUN<String> fun){
        return StreamUtils.mapList(maps, fun::getMapping);
    }

    //经常在 sql 中将字符串按照 , 进行合并
    public static List<String> splitList(String ids){
        if (!StringUtils.hasText(ids)){
            return new ArrayList<>();
        }
        return Av0.as(ids.split(","));
    }


    public static MethodWrapper getCurrentMethod(){
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        StackTraceElement current;
        if (stackTrace != null && stackTrace.length > 1){
            current = stackTrace[1];
            String name = current.getClassName();
            Class<?> currentClass;
            try {

                currentClass = Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("class not find", e);
            }
            ClassWrapper<?> wrapper = ClassWrapper.get(currentClass);
            return wrapper.getSingleMethod(current.getMethodName());
        }
        throw new RuntimeException("error get current method");
    }

    //0 : current 1 : textParseUtils
    public static int PARSE_TXT_USE_MODEL = 0;

    public static String parseTxt(String txt, @NonNull String start, @NonNull String end, @NonNull Function<String, String> function){
        switch (PARSE_TXT_USE_MODEL){
            case 0:
                return currentParseTxt(txt, start, end, function);
            case 1:
                return TextParseUtils.parse(start, end, txt, function);
            default:
                throw new IllegalStateException("ill model of parse txt :" + PARSE_TXT_USE_MODEL + " vaild val is 0 / 1");
        }
    }

    public static String currentParseTxt(String txt, @NonNull String start, @NonNull String end, @NonNull Function<String, String> function){
        if (!StringUtils.hasText(txt)) return txt;
        StringBuilder builder = new StringBuilder();
        String machingStr = txt;
        //首先判断参数填充
        //http:#{url}/#{mapping}/#{a}?value=#{value}&age=#{age}
        int processor = 0;
        int i = machingStr.indexOf(start);
        int lastEndIndex = 0;
        while (i != -1){
            for (;;){
                lastEndIndex = machingStr.indexOf(end);
                if (lastEndIndex > i) break;
                else {
                    int fi = lastEndIndex + end.length();
                    builder.append(machingStr, 0, fi);
                    machingStr = machingStr.substring(fi);
                    i = machingStr.indexOf(start);
                    processor = processor + fi;
                }
            }
            if (lastEndIndex == -1){
                throw new SQLSException("Missing Terminator: " + end);
            }

            if (i != 0){
                builder.append(machingStr, 0, i);
            }
            String key = machingStr.substring(i + start.length(), lastEndIndex);
            String apply = function.apply(key);
            builder.append(apply);
            int si = lastEndIndex + end.length();
            machingStr = machingStr.substring(si);
            processor = processor + si;
            i = machingStr.indexOf(start);
        }

        if (lastEndIndex != -1 && lastEndIndex != txt.length() - 1){
            builder.append(txt.substring(processor));
        }
        return builder.toString();
    }

    @Getter @Setter
    public static class SyntaxMethod{

        String methodName;
        int paramCount;
        Object[] args;
        List<String> paramNameList;
    }

    public static SyntaxMethod parseMethodTxt(String txt, Function<String, Object> getParamValue) {
        SyntaxMethod syntaxMethod = new SyntaxMethod();
        ArrayList<String> paramList = new ArrayList<>();
        int of = txt.indexOf("(");
        int ofend = txt.indexOf(")");
        if (of != -1 && ofend != -1 && ofend > of) {
            String paramString = txt.substring(of + 1, ofend);
            String methodName = txt.substring(0, of);
            String[] paramNames = paramString.split(",");
            Object[] args = new Object[paramNames.length];
            for (int i = 0; i < paramNames.length; i++) {
                String paramName = paramNames[i];
                Object apply = getParamValue.apply(paramName);
                args[i] = apply;
                paramList.add(paramName);
            }
            syntaxMethod.setMethodName(methodName);
            syntaxMethod.setArgs(args);
            syntaxMethod.setParamCount(paramList.size());
            syntaxMethod.setParamNameList(paramList);
            return syntaxMethod;
        }else {
            throw new IllegalStateException("error method style: " + txt);
        }
    }

    public static Object parseMethod(String txt, @NonNull Object source){
        int of = txt.indexOf("(");
        int ofend = txt.indexOf(")");
        while (of != -1 && ofend != -1 && ofend > of){
            String paramString = txt.substring(of + 1, ofend);
            String prefix = txt.substring(0, of);
            StringBuilder methodNameBuilder = new StringBuilder();
            for (int i = of - 1; i >= 0; i--) {
                char c = prefix.charAt(i);
                if (c == ' ' || i == 0){
                    if (i != 0 && i == of -1){
                        throw new IllegalStateException("no method name: " + prefix);
                    }
                    methodNameBuilder.append(i == 0 ? prefix : prefix.substring(i + 1));
                    break;
                }
            }
            //source.user.addAge
            String methodPath = methodNameBuilder.toString();
            String[] array = methodPath.split("\\.");
            //addAge
            String methodName = array[array.length - 1];
            StringBuilder beanPathBuilder = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                String ele = array[i];
                if (i != array.length - 1){
                    beanPathBuilder.append(ele);
                }
            }
            String beanPath = beanPathBuilder.toString();
            Object bean = findValue(source, beanPath);
            bean = bean == null ? source : bean;
            String afterParseBody;
            String[] paramNames = "".equals(paramString) ? new String[0] : paramString.split(",");
            Object[] args = new Object[paramNames.length];
            for (int i = 0; i < paramNames.length; i++) {
                String paramName = paramNames[i];
                Object value = findValue(source, paramName);
                args[i] = value;
            }
            Method method = findBeanMethod(methodName, bean, args.length);
            if (method == null){
                return null;
            }else {
                source = invokeBeanMethod(method, bean, args);
            }
            txt = txt.substring(ofend + 1);
            of = txt.indexOf("(");
            ofend = txt.indexOf(")");
        }
        return source;
    }

    public static String parseMethodTxt(String txt, Map<String, Object> source, String nullValue) {
        if (source == null){
            source = new HashMap<>();
        }
        int of = txt.indexOf("(");
        int ofend = txt.indexOf(")");
        StringBuilder txtBuilder = new StringBuilder();
        while (of != -1 && ofend != -1 && ofend > of){
            String paramString = txt.substring(of + 1, ofend);
            String prefix = txt.substring(0, of);
            StringBuilder methodNameBuilder = new StringBuilder();
            for (int i = of - 1; i >= 0; i--) {
                char c = prefix.charAt(i);
                if (c == ' ' || i == 0){
                    if (i != 0 && i == of -1){
                        throw new IllegalStateException("no method name: " + prefix);
                    }
                    txtBuilder.append(i == 0 ? "" : prefix.substring(0, i));
                    methodNameBuilder.append(i == 0 ? prefix : prefix.substring(i + 1));
                    break;
                }
            }
            //source.user.addAge
            String methodPath = methodNameBuilder.toString();
            String[] array = methodPath.split("\\.");
            //addAge
            String methodName = array[array.length - 1];
            StringBuilder beanPathBuilder = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                String ele = array[i];
                if (i != array.length - 1){
                    beanPathBuilder.append(ele);
                }
            }
            String beanPath = beanPathBuilder.toString();
            Object bean = findValue(source, beanPath);
            String afterParseBody;
            if (bean != null){
                String[] paramNames = "".equals(paramString) ? new String[0] : paramString.split(",");
                Object[] args = new Object[paramNames.length];
                for (int i = 0; i < paramNames.length; i++) {
                    String paramName = paramNames[i];
                    Object value = findValue(source, paramName);
                    args[i] = value;
                }
                Method method = findBeanMethod(methodName, bean, args.length);
                if (method == null){
                    afterParseBody = nullValue;
                }else {
                    Object result = invokeBeanMethod(method, bean, args);
                    afterParseBody = result == null ? bean.toString() : result.toString();
                }
            }else {
                afterParseBody = nullValue;
            }
            txtBuilder.append(afterParseBody);
            txt = txt.substring(ofend + 1);
            of = txt.indexOf("(");
            ofend = txt.indexOf(")");
        }
        txtBuilder.append(txt);
        return txtBuilder.toString();
    }

    public static Object invokeBeanMethod(Method method, Object bean, Object[] args){
        MethodWrapper mw = MethodWrapper.get(method);
        return mw.invoke(bean, args);
    }

    public static Method findBeanMethod(String methodName, Object bean, int argCount){
        Class<?> beanClass = bean.getClass();
        ClassWrapper<?> classWrapper = ClassWrapper.get(beanClass);
        MethodWrapper methodWrapper = classWrapper.getMethod(methodName, argCount);
        return methodWrapper == null ? null : methodWrapper.getMethod();
    }

    public static <T> T loopSetProperties(String name, Object value, T bean){
        return loopSetProperties(ServiceUtils.ofMap(name, value), bean);
    }

    //将 map 里的数据通过反射注入到实体类中, 主要想将集团id, 患者id注入到 嵌套的实体类中
    public static <T> T loopSetProperties(Map<String, Object> properties, T bean){
        if (properties == null || bean == null){
            return bean;
        }

        for (String fieldName : properties.keySet()) {
            Object value = properties.get(fieldName);
            setSingleProperties(bean, fieldName, value);
        }
        return bean;
    }

    public static void setSingleProperties(Object bean, String name, Object value){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
        ClassWrapper<Object> cw = ClassWrapper.get(primordialClass);
        if (Collection.class.isAssignableFrom(primordialClass)){
            Collection<?> collection = (Collection<?>) bean;
            for (Object ele : collection) {
                setSingleProperties(ele, name, value);
            }
        }else if (Map.class.isAssignableFrom(primordialClass)){
            Map<?, ?> map = (Map<?, ?>) bean;
            for (Object ele : map.values()) {
                setSingleProperties(ele, name, value);
            }
        }else {
            Collection<FieldWrapper> fields = cw.getFields();
            for (FieldWrapper field : fields) {
                Class<?> type = field.getType();
                if (Collection.class.isAssignableFrom(type)){
                    Collection<?> collection = (Collection<?>) field.getValue(bean);
                    if (collection != null){
                        for (Object unknown : collection) {
                            setSingleProperties(unknown, name, value);
                        }
                    }
                    continue;
                }

                if (Map.class.isAssignableFrom(type)){
                    Map<Object, Object> map = (Map<Object, Object>) field.getValue(bean);
                    if (map  != null){
                        for (Object unknown : map.values()) {
                            setSingleProperties(unknown, name, value);
                        }
                    }
                    continue;
                }

                if (field.hasAnnotation(Trust.class)){
                    setSingleProperties(field.getValue(bean), name, value);
                }else {
                    if (field.getName().equals(name)){
                        field.setValue(bean, value);
                    }
                }
            }
        }
    }
}
