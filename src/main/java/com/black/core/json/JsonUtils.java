package com.black.core.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.black.core.builder.Col;
import com.black.core.json.annotation.Matedata;
import com.black.core.json.world.ToJsonAlias;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BaseBean;
import com.black.core.tools.BeanUtil;
import com.black.core.util.SetGetUtils;
import com.black.core.util.StreamUtils;
import com.black.utils.ReflexHandler;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static com.black.core.json.ReflexUtils.getValue;
import static com.black.utils.ReflexHandler.getAccessibleFields;


@SuppressWarnings("all")
public final class JsonUtils {

    public final static String VALUE_KEY = "value";

    public static UCJsonParser ucJsonParser;

    static {
        ucJsonParser = new UCJsonParser();
    }

    /** 创建json */
    public static JSONObject buildJSON()  {return new JSONObject();}

    public static JSONObject buildJSON(Map<String, Object> map) {return new JSONObject(map);}

    public static JSONObject buildJSON(String text)  {return JSON.parseObject(text);}

    public static JSONObject ucParseJson(String text){
        if (ucJsonParser != null){
            return ucJsonParser.parseJson(text);
        }
        return null;
    }

    public static JSONObject vuParseJson(String text){
        if (!StringUtils.hasText(text)){
            return new JSONObject();
        }

        JSONObject object = new JSONObject();
        int start = text.indexOf("{");
        int end = text.indexOf("}");
        String body = text.substring(start + 1, end);
        String[] group = body.split(",");
        for (String g : group) {
            if (g.contains("=")){
                String[] keyAndValue = g.split("=");
                if (keyAndValue.length == 2){
                    object.put(keyAndValue[0].trim(), keyAndValue[1].trim());
                }
            }
        }
        return object;
    }

    public static JSONObject initJSON(String key, Object value){
        JSONObject json = buildJSON();
        json.put(key, value);
        return json;
    }

    /** 将map转换成json */
    public static JSONObject convertMapToJSON(Map<String, Object> map){
        final JSONObject json = new JSONObject();
        json.putAll(map);
        return json;
    }

    /** 载入json */
    public static JSONObject loadJSON(String key, Object value, JSONObject jsonObject){
        jsonObject.put(key, value);
        return jsonObject;
    }

    public static JSONArray buildArray(){return new JSONArray();}

    public static JSONArray buildArray(List<?> array){
        return new JSONArray((List<Object>) array);
    }

    public static <T> Collection<T> toBeanArray(JSONArray array, Class<T> beanClass, boolean complete){
        if (array == null){
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>();
        for (Object a : array) {
            JSONObject json = (JSONObject) JSON.toJSON(a);
            T bean = toObject(json, ReflexUtils.instance(beanClass));
            if (bean instanceof BaseBean){
                if (complete){
                    BaseBean<?> baseBean = (BaseBean<?>) bean;
                    baseBean.complete();
                }
            }
            result.add(bean);
        }
        return result;
    }

    public static void judgeNotNull(Object pojo){
        getAccessibleFields(pojo)
                .forEach(
                        f ->{
                            if (AnnotationUtils.getAnnotation(f, NotNull.class) == null)
                                return;
                            try {
                                if (f.get(pojo) == null)
                                    throw new RuntimeException("字段:" + f +"不能为空");
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException("get field : "+ f + "fail");
                            }
                        }
                );
    }

    public static JSONArray createArray(String text)
    {
        return JSONArray.parseArray(text);
    }

    /* 参数 aliasReplaceKey: 遇到别名注解时,是否替换key */
    public static JSONObject jsonFilter(JSONObject source, Class<?> sample, boolean aliasReplaceKey){

        if (source == null)
            return buildJSON();
        if (sample == null)
            return source;
        getAccessibleFields(sample).forEach(
                f ->{
                    if (!source.containsKey(f.getName()))
                        return;
                    //对标注了 ignore 的属性进行过滤
                    if (AnnotationUtils.getAnnotation(f, Ignore.class) != null)
                    {
                        source.remove(f.getName());
                        return;
                    }
                    //如果添加了别名的注解
                    Alias alias = AnnotationUtils.getAnnotation(f, Alias.class);
                    if (alias != null && aliasReplaceKey) {
                        String value = alias.value();
                        Object val = source.get(f.getName());
                        source.remove(f.getName());
                        source.put(value, val);
                    }
                }
        );

        return source;
    }
    public static <V> JSONArray convertList(List<V> javaList, Class<? extends Annotation> annotationClass){
        if(javaList == null || javaList.isEmpty())
            return new JSONArray();
        JSONArray array = new JSONArray();
        javaList.forEach(
                obj-> array.add(toJson(obj))
        );
        return array;
    }

    public static JSONArray converjsonArray(List<Map<String, Object>> list, Class<?> pojoClass){

        if (list.isEmpty())
            return buildArray();
        List<JSONObject> objectList = list.stream().map(
                m -> jsonFilter(buildJSON(m), pojoClass, true)
        ).collect(Collectors.toList());
        return buildArray(objectList);
    }
    public static JSONObject convert(Object pojo){
        return convert(pojo, Alias.class, false, Object.class);
    }

    public static JSONObject convert(Object pojo, Class<? extends Annotation> annotationClass){
        return convert(pojo, annotationClass, false, Object.class);
    }
    /** 将实体类转换成json */
    public static JSONObject convert(Object pojo, Class<? extends Annotation> annotationClass,
                              boolean scanSuper,
                              Class<?> superClass)
    {
        if(pojo == null)
            return new JSONObject();
        final JSONObject jsonObject = new JSONObject();
        /* 遍历实体类所有字段 */
        for (Field field : getAccessibleFields(pojo, scanSuper, superClass)) {
            //判断字段上是否含有传入的注解
            Annotation a = AnnotationUtils.getAnnotation(field, annotationClass);
            if (a != null){
                //拿到注解
                String key = (String) com.black.core.util.AnnotationUtils.getValueFromAnnotation(a);
                /* 拿到 value 值，来当作新jsonObject的key */
                /* 拿到字段的值 */
                Object value = getValue(field, pojo);
                if (value != null)  /* 存到json中 */
                    jsonObject.put(key, value);
            }
        }
        return jsonObject;
    }

    public static JSONObject letV2Json(Object bean){
        Object json = JSON.toJSON(bean);
        if (json == null){
            return new JSONObject();
        }
        if (json instanceof JSONObject){
            return (JSONObject) json;
        }

        if (json instanceof JSONArray){
            throw new JsonParseException("parse bean to json, but is array");
        }
        throw new JsonParseException("can not parse object to json:" + bean);
    }

    public static JSONObject letJson(Object bean){
        if (bean == null) return new JSONObject();
        if(bean instanceof JSONObject){
            return (JSONObject) bean;
        }

        if (bean instanceof Map){
            return new JSONObject((Map<String, Object>) bean);
        }

        if (bean instanceof String){
            return JSONObject.parseObject((String) bean);
        }

        Class<Object> primordialClass = BeanUtil.getPrimordialClass(bean);
        ClassWrapper<Object> wrapper = ClassWrapper.get(primordialClass);
        JSONObject json = new JSONObject();
        for (FieldWrapper fieldWrapper : wrapper.getFields()) {
            if (fieldWrapper.hasAnnotation(Ignore.class)){
                continue;
            }
            Field rawField = fieldWrapper.getField();
            Class<?> type = fieldWrapper.getType();
            String typeName = type.getName();
            if (SetGetUtils.hasGetMethod(rawField)) {
                Object value = SetGetUtils.invokeGetMethod(rawField, bean);
                //如果值是一个 map
                if (value instanceof Map){
                    Map<Object, Object> map = (Map<Object, Object>) value;
                    //遍历 map
                    for (Object key : map.keySet()) {
                        Object val = map.get(key);
                        if (val != null){
                            //如果值不等于空, 并且可以再次解析
                            Class<Object> pc = BeanUtil.getPrimordialClass(val);
                            String pcName = pc.getName();
                            if (!typeName.startsWith("java") && !ClassWrapper.isBasic(typeName)){
                                if (type.isAnnotationPresent(Trust.class)) {
                                    map.replace(key, letJson(value));
                                }
                            }
                        }
                    }
                }else
                if (!typeName.startsWith("java") && !ClassWrapper.isBasic(typeName)){
                    if (type.isAnnotationPresent(Trust.class)) {
                        value = letJson(value);
                    }
                }
                else
                if (value instanceof Collection){
                    try {
                        Class<?>[] genericVal = ReflexHandler.genericVal(rawField, Collection.class);
                        if (genericVal.length == 1){
                            Class<?> genClass = genericVal[0];
                            String className = genClass.getName();
                            if (!className.startsWith("java")){
                                ClassWrapper<?> classWrapper = ClassWrapper.get(genClass);
                                if (classWrapper.hasAnnotation(Trust.class)) {
                                    value = StreamUtils.mapList((Collection<? extends Object>) value, JsonUtils::letJson);
                                }
                            }
                        }
                    }catch (Throwable e){
                        //ignore
                    }
                }
                json.put(fieldWrapper.getName(), value);
            }
        }
        return json;
    }

    public static <B> B toBean(JSONObject json, Class<B> beanType){
        return toBean(json, beanType, false, false);
    }

    public static <B> B toBean(JSONObject json, Class<B> beanType, boolean setNull, boolean nicely){
        B bean = ReflexUtils.instance(beanType);
        if (json == null){
            return bean;
        }
        ClassWrapper<B> wrapper = ClassWrapper.get(beanType);
        for (FieldWrapper fieldWrapper : wrapper.getFields()) {
            Object val = null;
            Class<?> type = fieldWrapper.getType();
            String name = fieldWrapper.hasAnnotation(Alias.class) ? fieldWrapper.getAnnotation(Alias.class).value() : fieldWrapper.getName();
            if (type.isAnnotationPresent(Trust.class)){
                if(beanType.isAssignableFrom(type)){
                    throw new JsonParseException("目标 bean 存在相同类型的引用, 会引起死循环");
                }
                if (nicely){
                    //则属性 bean 的属性存在源数据 json 中
                    val = toBean(json, type, setNull, nicely);
                }else {
                    if (json.containsKey(name)) {
                        Object subVal = json.get(name);
                        if (subVal instanceof Map){
                            val = toBean(new JSONObject((Map<String, Object>) subVal), type, setNull, nicely);
                        }
                    }
                }

            }else {
                if (json.containsKey(name)){
                    val = json.get(name);
                }
            }
            if (val != null || setNull){
                Object value = val;
                try {
                    SetGetUtils.invokeSetMethod(fieldWrapper.getField(), value, bean);
                }catch (RuntimeException e){
                    throw new JsonParseException("error for set value in field: [" + name + "] of bean: [" + beanType.getSimpleName() + "]", e);
                }
            }
        }
        return bean;
    }

    public static <T> T letBean(JSONObject json, @NotNull Class<T> entityType){
        if (json == null){
            return ReflexUtils.instance(entityType);
        }
        T instance = ReflexUtils.instance(entityType);
        if (Map.class.isAssignableFrom(entityType)){
            Map<String, Object> map = (Map<String, Object>) instance;
            map.putAll(json);
            return (T) map;
        }
        ClassWrapper<T> cw = ClassWrapper.get(entityType);
        for (FieldWrapper fw : cw.getFields()) {
            String name = fw.getName();
            if (!SetGetUtils.hasSetMethod(fw.get())){
                //no set
                continue;
            }
            if (!json.containsKey(name)){
                // no exist no set null
                setMatedataField(fw, instance, json);
                continue;
            }
            Object value = json.get(name);
            if (value == null){
                //set null
                fw.setNullValue(instance);
                continue;
            }

            Class<?> fwType = fw.getType();
            if (Map.class.isAssignableFrom(fwType)){
                if (value instanceof Map){
                    fw.setValue(instance, value);
                }
            }else if (Collection.class.isAssignableFrom(fwType)){
                List<Object> list = SQLUtils.wrapList(value);
                Class<?>[] genericVal = ReflexHandler.genericVal(fw.get(), fwType);
                if (genericVal.length != 1){
                    throw new IllegalStateException("let field: " + name + " fair, because can not get generic val");
                }
                Class<?> genericType = genericVal[0];
                Collection collectionInstance = getCollectionInstance(fwType);
                //遍历每个元素数据
                for (Object eleVal : list) {
                    JSONObject letJson = letJson(eleVal);
                    Object bean = letBean(letJson, genericType);
                    collectionInstance.add(bean);
                }
                fw.setValue(instance, collectionInstance);
            }else {
                if (com.black.core.util.AnnotationUtils.findAnnotation(fwType, Trust.class) != null){
                    JSONObject letJson = letJson(value);
                    Object bean = toBean(letJson, fwType, false, false);
                    SetGetUtils.invokeSetMethod(fw.get(), bean, instance);
                }else {
                    SetGetUtils.invokeSetMethod(fw.get(), value, instance);
                }
            }
        }
        return instance;
    }

    public static <T extends Collection> T getCollectionInstance(@NotNull Class<?> unknownType){
        if (unknownType.isArray()){
            return (T) new ArrayList<Object>();
        }
        if (!Collection.class.isAssignableFrom(unknownType)){
            throw new IllegalArgumentException("unknown type is not collection");
        }
        if (BeanUtil.isSolidClass(unknownType)){
            return (T) ReflexUtils.instance(unknownType);
        }
        if (List.class.isAssignableFrom(unknownType)){
            return (T) new ArrayList<Object>();
        }
        if (Set.class.isAssignableFrom(unknownType)){
            return (T) new HashSet<Object>();
        }
        if (Queue.class.isAssignableFrom(unknownType)){
            return (T) new LinkedBlockingQueue<Object>();
        }
        throw new IllegalStateException("ill instance collection by type: " + unknownType.getSimpleName());
    }

    private static void setMatedataField(FieldWrapper fw, Object bean, JSONObject json){
        Class<?> fwType = fw.getType();
        if (Map.class.isAssignableFrom(fwType) && fw.hasAnnotation(Matedata.class)){
            if (BeanUtil.isSolidClass(fwType)){
                Object instance = ReflexUtils.instance(fwType);
                Map<String, Object> map = (Map<String, Object>) instance;
                map.putAll(json);
                fw.setValue(bean, map);
            }else {
                fw.setValue(bean, json);
            }
        }
    }

    public static <T> T toObject(JSONObject json, Class<T> pojoClass){
        return toObject(json, ReflexUtils.instance(pojoClass), false, Object.class, Alias.class);
    }

    /** 将json 转成pojo */
    public static <T> T toObject(JSONObject json, T pojo){
        return toObject(json, pojo, false, Object.class, Alias.class);
    }

    /** 将json 转成pojo */
    public static <T> T toObject(JSONObject json, T pojo, boolean scanSuper, Class<?> superClass) {
        return toObject(json, pojo, scanSuper, superClass, Alias.class);
    }

    /** 将json 转成pojo */
    public static <T> T toObject(JSONObject json, T pojo, Class<? extends Annotation> annotationClass){
        return toObject(json, pojo, false, Object.class, annotationClass);
    }

    /***
     * 将 json 转换成实体类
     * @param json json 对象
     * @param pojo 实体类对象
     * @param scanSuper 是否扫描到父类
     * @param superClass 顶级父类
     * @param annotationClass 别名注解
     * @param <T> 泛型
     * @return 返回实体类
     */
    public static <T> T toObject(JSONObject json,T pojo, boolean scanSuper,
                              Class<?> superClass,
                              Class<? extends Annotation> annotationClass)
    {
        if (json == null)
            return pojo;
        if (pojo == null)
            return null;
        final Class<?> pojoClass = pojo.getClass();
        Map<String, String> uk = Col.uk(json);
        for (Field field : getAccessibleFields(pojo, scanSuper, superClass)) {
            String name, methodInfo = null;
            Annotation annotation = null;
            Annotation ann = AnnotationUtils.getAnnotation(field, annotationClass);
            if (ann != null){
                name = (String) com.black.core.util.AnnotationUtils.getValueFromAnnotation(ann);
            }else {
                name = field.getName();
            }
            Object value = json.get(uk.get(Col.u(name)));
            //根据名字去json中获取value
            if (value != null){
                ReflexUtils.setValue(field, pojo, value);
            }
        }
        return pojo;
    }

    //--------------------------------------------------
    //              pojo to json
    //---------------------------------------------------
    public static JSONObject toJson(Object pojo)
    {return toJson(pojo, true);}

    public static JSONObject toJson(Object pojo, Boolean ignoreNull){
        return toJson(pojo, ignoreNull, false, Object.class);
    }

    public static JSONObject toJson(Object pojo, Boolean ignoreNull, boolean scanSuper){
        return toJson(pojo, ignoreNull, scanSuper, Object.class);
    }


    /***
     * 将一个实体类转换成 json 格式
     * @param pojo 实体类对象
     * @param ignoreNull 是否忽略空值
     * @param scanSuper 是否扫描其父类
     * @param superClass 如果扫描到父类, 能到达的最顶级父类
     * @return 返回 json
     */
    public static JSONObject toJson(Object pojo, Boolean ignoreNull, boolean scanSuper, Class<?> superClass)
    {
        if (pojo instanceof Map){
            return new JSONObject((Map<String, Object>) pojo);
        }
        if (pojo == null)
            return new JSONObject();

        Map<String, Object> map = new HashMap<>();
        for (Field field : getAccessibleFields(pojo, scanSuper, superClass)) {
            if (AnnotationUtils.getAnnotation(field, Ignore.class) != null)
                continue;
            Object value = getValue(field, pojo);
            if (value == null && ignoreNull){
                continue;
            }
            Anatomy anatomy = AnnotationUtils.getAnnotation(field, Anatomy.class);
            if (anatomy != null){
                value = anatomyObj(value, anatomy, ignoreNull);
            }
            ToJsonAlias alias = AnnotationUtils.getAnnotation(field, ToJsonAlias.class);
            String key = alias == null ? field.getName() : alias.value();
            map.put(key, value);
        }
        return new JSONObject(map);
    }


    public static JSONArray convertResponseWithCollection(@NotNull Collection<?> collection){
        return new JSONArray(
                collection.stream().map(c ->toJson(c, true, false, Object.class)).collect(Collectors.toList())
        );
    }

    /***
     * 深度的去解析一个字段
     * @param val 字段的 value
     * @param anatomy 该字段上标注的注解
     * @return 返回 json
     */
    static Object anatomyObj(Object val, Anatomy anatomy, boolean ignoreNull){
        boolean up = anatomy != null && anatomy.upAnalysis();
        Class<?> topClass = up ? anatomy.topClass() : Object.class;

        if (val instanceof ToJsonObject){
            ToJsonObject toJsonObject = (ToJsonObject) val;
            return toJsonObject.toJson();
        }

        //特殊情况: map
        if (val instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) val;
            map.keySet().removeIf(o -> map.get(o) == null && ignoreNull);
            map.keySet().forEach(
                    n ->{
                        Object deepVal = map.get(n);
                        //可以解析的 class 上需要添加信任注解
                        if (deepVal != null && AnnotationUtils.getAnnotation(deepVal.getClass(), Trust.class) != null)
                            //解析完成后,替换成新值
                            map.put(n, anatomyObj(deepVal,
                                    AnnotationUtils.getAnnotation(deepVal.getClass(), Anatomy.class), ignoreNull));
                    }
            );
            return map;
        }

        //特殊情况: list
        if (val instanceof List) {
            List<Object> list = (List<Object>) val;
            return list.stream().map(
                    e -> {

                        if (e != null && AnnotationUtils.getAnnotation(e.getClass(), Trust.class) == null)
                            return e;
                        return anatomyObj(e, AnnotationUtils.getAnnotation(e.getClass(), Anatomy.class), ignoreNull);
                    }
            ).collect(Collectors.toList());
        }
        return toJson(val, ignoreNull, up, topClass);
    }

    public static String toJsonString(Object source){
        return toJsonString(toJson(source, true));
    }

    public static String toJsonString(JSONObject json){
        return toJsonString(true, json);
    }

    public static String toJsonString(boolean filterNull, Object source){
        JSONObject responseJson = toJson(source, filterNull);
        return toJsonString(filterNull, responseJson);
    }

    public static String toJsonString(boolean filterNull, JSONObject json){
        return filterNull ? json.toString() : JSONObject.toJSONString(json, SerializerFeature.WriteMapNullValue);
    }
}
