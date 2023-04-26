package com.black.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.black.api.handler.*;
import com.black.core.bean.TrustBeanCollector;
import com.black.core.json.ReflexUtils;
import com.black.core.json.Trust;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.javassist.*;
import com.black.template.jdbc.JavaColumnMetadata;
import com.black.template.jdbc.JdbcType;
import com.black.utils.IdUtils;
import com.black.utils.ReflexHandler;
import com.black.utils.ServiceUtils;
import lombok.Getter;
import org.springframework.web.bind.annotation.RequestPart;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@SuppressWarnings("all")
public class ApiV2Utils {

    public static String HTTP_METHOD_NAME = "HTTP-METHOD";

    public static final String RESPONSE_PREFIX = "response: ";

    public static final String RESPONSE_CLASS_NAME = "RESPONSE_CLASS_NAME";

    public static final String CONNECTION_NAME = "Connection-jdbc";

    public static final String ALIAS_COLUMN_NAME = "Alias-column";

    public static final String CONTROLLER_TYPE_NAME = "Controller-type";

    public static final String METHOD_WRAPPER_NAME = "Wrapper-method";

    public static final String DEFAULT_FLAG = "$R:";

    public static boolean remarkJoin = true;
    
    public static Set<String> requestExcludes = new HashSet<>();

    public static Map<String, Supplier<String>> staticColumnValueMap = new ConcurrentHashMap<>();

    private static final LinkedBlockingQueue<MetadataBuilder> obtainMatedataFunctionQueue = new LinkedBlockingQueue<>();

    private static final LinkedBlockingQueue<MetadataResolver> metadataResolverQueue = new LinkedBlockingQueue<>();


    static {
        obtainMatedataFunctionQueue.add(new TrustBeanMetadataBuilder());
        obtainMatedataFunctionQueue.add(new TableMetadataBuilder());
        metadataResolverQueue.add(new TableMetadataResolver());
        metadataResolverQueue.add(new TrustBeanMetadataResolver());
    }

    public static final ColumnAnnotationGenerator DEF_ANN_COLUMN_GENERATOR = new ColumnAnnotationGenerator() {
        @Override
        public CtAnnotations createAnnotations(JavaColumnMetadata javaColumnMetadata) {
            CtAnnotations ctAnnotations = new CtAnnotations();
            CtAnnotation ctAnnotation = new CtAnnotation(ApiRemark.class);
            String remarks = javaColumnMetadata.getRemarks();
            ctAnnotation.addField("value", StringUtils.hasText(remarks) ? remarks : "", String.class);
            ctAnnotations.addAnnotation(ctAnnotation);
            return ctAnnotations;
        }
    };

    public static Connection wrapperConnection(Connection connection){
        return new DatabaseUniquenessConnectionWrapper(connection);
    }

    public static JSONObject castClassToRequestJsonExplain(Class<?> type){
        JSONObject jsonObject = new JSONObject();
        ClassWrapper<?> wrapper = ClassWrapper.get(type);
        Collection<FieldWrapper> fields = wrapper.getFields();
        for (FieldWrapper field : fields) {
            Class<?> fieldType = field.getType();
            String fieldTypeName = fieldType.getName();
            String fieldName = field.getName();

            if (fieldTypeName.startsWith(Utils.FICTITIOUS_PATH) || fieldType.isAssignableFrom(Trust.class)){
                jsonObject.put(fieldName, castClassToRequestJsonExplain(fieldType));
                continue;
            }

            if (Collection.class.isAssignableFrom(fieldType)){
                JSONArray array = new JSONArray();
                Class<?>[] genericVal = ReflexHandler.genericVal(field.getField(), fieldType);
                if (genericVal.length == 1){
                    array.add(castClassToRequestJsonExplain(genericVal[0]));
                }
                jsonObject.put(fieldName, array);
                continue;
            }
            ApiRemark annotation = field.getAnnotation(ApiRemark.class);
            String remark = annotation == null ? "" : annotation.value();
            remark = remark + "(" + field.getType().getSimpleName() + ")";
            jsonObject.put(fieldName, remark);
        }
        return jsonObject;
    }

    public static JSONObject castClassToRequestJsonDemo(Class<?> type){

        JSONObject jsonObject = new JSONObject();
        ClassWrapper<?> wrapper = ClassWrapper.get(type);
        Collection<FieldWrapper> fields = wrapper.getFields();
        for (FieldWrapper field : fields) {
            Class<?> fieldType = field.getType();
            String fieldTypeName = fieldType.getName();
            String fieldName = field.getName();

            if (fieldTypeName.startsWith(Utils.FICTITIOUS_PATH) || fieldType.isAssignableFrom(Trust.class)){
                jsonObject.put(fieldName, castClassToRequestJsonDemo(fieldType));
                continue;
            }

            if (Collection.class.isAssignableFrom(fieldType)){
                JSONArray array = new JSONArray();
                Class<?>[] genericVal = ReflexHandler.genericVal(field.getField(), fieldType);
                if (genericVal.length == 1){
                    array.add(castClassToRequestJsonDemo(genericVal[0]));
                }
                jsonObject.put(fieldName, array);
                continue;
            }
            Object remark = getDemoByType(fieldType);
            jsonObject.put(fieldName, remark);
        }
        return jsonObject;
    }

    public static Object getDemoByType(Class<?> type){
        if (boolean.class.equals(type) || Boolean.class.equals(type)){
            return false;
        }else if (Integer.class.equals(type) || int.class.equals(type)){
            return 0;
        }else if (String.class.equals(type)){
            return "example";
        }else if (Number.class.isAssignableFrom(type)){
            return 0;
        }else if (Map.class.isAssignableFrom(type)){
            return "{}";
        }else if (Collection.class.isAssignableFrom(type)){
            return "[]";
        }else {
            if (TrustBeanCollector.existTrustBean(type)) {
                JSONObject json = new JSONObject();
                ClassWrapper<?> cw = BeanUtil.getPrimordialClassWrapper(type);
                Collection<FieldWrapper> fields = cw.getFields();
                for (FieldWrapper field : fields) {
                    Class<?> fieldType = field.getType();
                    Object demoByType = getDemoByType(fieldType);
                    json.put(field.getName(), demoByType);
                }
                return json.toString(SerializerFeature.WriteMapNullValue);
            }
        }
        return "example";
    }

    protected static String parsePlane(String plane, Class<?> type){
        ClassWrapper<?> wrapper = ClassWrapper.get(type);
        AtomicReference<Object> beanRef = new AtomicReference<>();
        return ServiceUtils.parseTxt(plane, "$<", ">", name -> {
            MethodWrapper method = wrapper.getSingleMethod(name);
            if (method != null){
                Object bean = beanRef.get();
                if (bean == null){
                    beanRef.set(ReflexUtils.instance(type));
                    bean = beanRef.get();
                    Object invoke = method.invoke(bean);
                    if (invoke != null){
                        return invoke.toString();
                    }
                }
            }
            return "";
        });
    }

    public static String createBoundary(){
        return "-----------------------------" + IdUtils.createShort22Id();
    }

    public static boolean isMuiltPartRequest(HttpMethod method, MethodWrapper methodWrapper){
        List<String> requestMethod = method.getRequestMethod();
        boolean allowPost = requestMethod.contains("POST");
        if (allowPost){
            return methodWrapper.parameterHasAnnotation(RequestPart.class);
        }
        return false;
    }

    protected static void setRemarkOfMethod(MethodWrapper mw, HttpMethod method, String defaultRemark){
        if (StringUtils.hasText(defaultRemark)){
            method.setRemark(defaultRemark);
        }else {
            List<String> requestUrl = method.getRequestUrl();
            StringJoiner joiner = new StringJoiner(" or ");
            for (String url : requestUrl) {
                joiner.add(url);
            }
            method.setRemark("url: " + joiner.toString());
        }
    }

    public static JSONArray parseBlentArray(JDBCBlent blent, Connection connection, AliasColumnConvertHandler handler, boolean request, Class<?> type){
        JSONArray array = new JSONArray();
        JSONObject object = parseBlent(blent, connection, handler, request, type);
        array.add(object);
        return array;
    }

    public static Object getMetadata(String name, Connection connection){
        Object matedata = null;
        for (MetadataBuilder matedataBuilder : obtainMatedataFunctionQueue) {
            matedata = matedataBuilder.buildMatedata(name, connection);
            if (matedata != null){
                break;
            }
        }
        return matedata;
    }

    public static JSONObject parseBlent(JDBCBlent blent, Connection connection,
                                        AliasColumnConvertHandler handler, boolean request, Class<?> type){
        JSONObject json = new JSONObject();
        //按照 first 来说, plans 是平坦的字段
        List<String> planes = blent.planes;
        for (String plane : planes) {
            plane = parsePlane(plane, type);
            //String tableName = handler.convertColumn(plane);
            Object matedata = getMetadata(plane, connection);
            if (matedata == null){
                continue;
            }

            processorJson(matedata, json, handler, request);
        }
        for (JDBCBlent object : blent.blendObjects) {
            loop(json, object, connection, handler, request, type);
        }
        return json;
    }

    public static void loop(JSONObject json, JDBCBlent blent, Connection connection,
                            AliasColumnConvertHandler handler,
                            boolean request, Class<?> type){
        List<String> planes = blent.planes;
        if (planes.size() == 1){
            String p = planes.get(0);
            p = parsePlane(p, type);
            Object matedata = null;
            for (MetadataBuilder matedataBuilder : obtainMatedataFunctionQueue) {
                matedata = matedataBuilder.buildMatedata(p, connection);
                if (matedata != null){
                    break;
                }
            }
            if (matedata == null){
                return;
            }
            JSONObject sonJson = new JSONObject();
            if (blent.json) {
                json.put(blent.alias, sonJson);
                processorJson(matedata, sonJson, handler, request);
            }else {
                JSONArray array = new JSONArray();
                json.put(blent.alias, array);
                processorJson(matedata, sonJson, handler, request);
                array.add(sonJson);
            }
            for (JDBCBlent object : blent.blendObjects) {
                loop(sonJson, object, connection, handler, request, type);
            }
        }else {
            throw new IllegalStateException("loop plane must = 1");
        }

    }

    public static void processorJson(Object metadata,
                                     JSONObject sonJson,
                                     AliasColumnConvertHandler handler, boolean request){
        for (MetadataResolver resolver : metadataResolverQueue) {
            if (resolver.support(metadata)){
                resolver.resolve(metadata, sonJson, handler, request);
            }
        }
    }

    public static void wriedRemark(JSONObject json, String column, Class<?> javaClass, String remark, JdbcType jdbcType, boolean request){
        String value = request ? StringUtils.linkStr(remark, "(", javaClass == null ? "String" : javaClass.getSimpleName(), " 可选 )")
                : StringUtils.linkStr(remark, "(", javaClass == null ? jdbcType.getName() : javaClass.getSimpleName(), ")");

        json.put(column, value);
    }

    public static void configRequestExclude(String... column){
        requestExcludes.addAll(Arrays.asList(column));
    }

    public static void wriedDate(JSONObject json, String column){
        json.put(column, ServiceUtils.now("yyyy-MM-dd HH:mm:ss"));
    }

    public static void wriedString(JSONObject json, String column, Class<?> javaClass){
        if (staticColumnValueMap.containsKey(column)){
            json.put(column, staticColumnValueMap.get(column).get());
            return;
        }
        json.put(column, "example");
    }

    public static String wriedString(String column){
        if (staticColumnValueMap.containsKey(column)){
            return staticColumnValueMap.get(column).get();
        }
        return "example";
    }

    public static void wriedInt(JSONObject json, String column){
        json.put(column, 0);
    }

    public static void writeBoolean(JSONObject json, String column){
        json.put(column, false);
    }

    //supplier, driver[supplierAddress(address){}, supplierLicence(licenceList)[]]
    @Getter
    public static class JDBCBlent{

        //平面 = supplier, driver
        List<String> planes = new ArrayList<>();
        // = [] / {}
        boolean json;

        //子数据
        List<JDBCBlent> blendObjects = new ArrayList<>();

        //别名
        String alias;


        public void add(JDBCBlent blendObjects){
            if (blendObjects != null){
                this.blendObjects.add(blendObjects);
            }
        }
    }

    enum STATE{
        FIRST,
        LOOP,
        BUILD
    }

    //supplier, driver[supplierAddress(address){person(person){}}, supplierLicence(licenceList)[]]
    public static JDBCBlent parseBlends(String context){
        if (!StringUtils.hasText(context)){
            return null;
        }
        JDBCBlent blent = new JDBCBlent();
        StringBuilder name = new StringBuilder();
        JDBCBlent[] fathers = new JDBCBlent[16];
        fathers[0] = blent;
        STATE state = STATE.FIRST;
        int step = 0;
        for (char chr : context.toCharArray()) {
            if (chr == ' ') continue;
            if (chr == '\n') continue;
            if (chr == ',' ){
                if (name.length() > 0){
                    switch (state){
                        case FIRST:
                            fathers[step].planes.add(name.toString());
                            break;
                        default:
                            throw new IllegalStateException("异常状态:[" + state + "] 操作符: [,] 文本:" + context);
                    }
                    name.delete(0, name.length());
                }
            }
            else
            if (chr == '['){
                switch (state){
                    case FIRST:
                        fathers[step].json = false;
                        if (name.length() > 0){
                            fathers[step].planes.add(name.toString());
                        }
                        state = STATE.LOOP;
                        break;
                    case BUILD:
                        fathers[step].json = false;
                        state = STATE.LOOP;
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: ['['] 文本:" + context);
                }
                name.delete(0, name.length());
            }
            else
            if (chr == ']'){
                switch (state){
                    case LOOP:
                        if (step > 0){
                            fathers[step - 1].blendObjects.add(fathers[step]);
                            fathers[step--] = null;
                        }
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: [']'] 文本:" + context);
                }
                name.delete(0, name.length());
            }
            else if(chr == '('){
                switch (state){
                    case LOOP:
                        JDBCBlent jdbcBlent = new JDBCBlent();
                        fathers[++step] = jdbcBlent;
                        jdbcBlent.planes.add(name.toString());
                        state = STATE.BUILD;
                        break;
                    case BUILD:
                        fathers[step].planes.add(name.toString());
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: ['('] 文本:" + context);
                }
                name.delete(0, name.length());
            }else if(chr == ')'){
                switch (state){
                    case BUILD:
                        fathers[step].alias = name.toString();
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: [')'] 文本:" + context);
                }
                name.delete(0, name.length());
            }else if (chr == '{'){
                switch (state){
                    case FIRST:
                        fathers[step].json = true;
                        if (name.length() > 0){
                            fathers[step].planes.add(name.toString());
                        }
                        state = STATE.LOOP;
                        break;
                    case LOOP:
                        JDBCBlent jdbcBlent = new JDBCBlent();
                        fathers[++step] = jdbcBlent;
                        jdbcBlent.planes.add(name.toString());
                        state = STATE.BUILD;
                    case BUILD:
                        fathers[step].json = true;
                        state = STATE.LOOP;
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: ['{'] 文本:" + context);
                }
                name.delete(0, name.length());
            }else if (chr == '}'){
                switch (state){
                    case LOOP:
                        if (step > 0){
                            fathers[step - 1].blendObjects.add(fathers[step]);
                            fathers[step--] = null;
                        }
                        break;
                    default:
                        throw new IllegalStateException("异常状态:[" + state + "] 操作符: ['}'] 文本:" + context);
                }
                name.delete(0, name.length());
            }else {
                name.append(chr);
            }
        }
        return blent;
    }

}
