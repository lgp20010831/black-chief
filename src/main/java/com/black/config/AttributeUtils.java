package com.black.config;

import com.alibaba.fastjson.JSONObject;
import com.black.pattern.NameAndValue;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.json.JsonUtils;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.ClassUtils;
import com.black.core.util.SetGetUtils;
import com.black.core.util.Utils;

import java.lang.reflect.Method;
import java.util.*;

public class AttributeUtils {

    public static final IoLog log = LogFactory.getArrayLog();

    public static String getName(Object bean){
        return BeanUtil.getPrimordialClass(bean).getSimpleName();
    }

    public static <T> T mappingBean(Object dataBean, T targetBean){
        if (dataBean == null || targetBean == null) return targetBean;
        JSONObject json = JsonUtils.letJson(dataBean);
        return mapping(targetBean, json);
    }

    public static <T> T mapping(T bean, Map<String, Object> source){
        ClassWrapper<?> wrapper = ClassUtils.getClassWrapper(BeanUtil.getPrimordialClass(bean));
        for (String fieldName : wrapper.getFieldNames()) {
            if (source.containsKey(fieldName)) {
                Object val = source.get(fieldName);
                if (val != null){
                    log.trace("[CONFIG] mapping attribute: {} ====> field: [{}]",
                            val, fieldName);
                    SetGetUtils.invokeSetMethod(fieldName, val, bean);
                }
            }
        }
        return bean;
    }

    public static Map<Class<?>, List<NameAndValue>> collectAttributes(Object bean){
        ClassWrapper<?> wrapper = BeanUtil.getPrimordialClassWrapper(bean);
        Collection<FieldWrapper> fields = wrapper.getFields();
        Map<Class<?>, List<NameAndValue>> map = new LinkedHashMap<>();
        for (FieldWrapper field : fields) {
            Class<?> type = field.getType();
            Object value = field.getValue(bean);
            NameAndValue nameAndValue = new NameAndValue();
            nameAndValue.setName(field.getName());
            nameAndValue.setValue(value);
            List<NameAndValue> nameAndValues = map.computeIfAbsent(type, t -> new ArrayList<>());
            nameAndValues.add(nameAndValue);
        }
        return map;
    }

    public static Map<Class<?>, List<NameAndValue>> merge(Map<Class<?>, List<NameAndValue>> source,
                                                          Map<Class<?>, List<NameAndValue>> target){

        for (Class<?> type : source.keySet()) {
            List<NameAndValue> values = target.remove(type);
            if (values != null){
                List<NameAndValue> nameAndValues = source.get(type);
                nameAndValues.addAll(values);
            }
        }
        source.putAll(target);
        return source;
    }


    public static Object[] mergeArgs(MethodWrapper mw, Method sourceMethod, Object[] args, Map<Class<?>, List<NameAndValue>> attr){
        Collection<ParameterWrapper> pws = mw.getParameterWrappersSet();
        Object[] newArgs = new Object[mw.getParameterCount()];
        MethodWrapper smw = MethodWrapper.get(sourceMethod);
        loopPw: for (ParameterWrapper pw : pws) {
            Class<?> type = pw.getType();
            String name = pw.getName();
            int index = pw.getIndex();
            //优先匹配原生参数
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                ParameterWrapper spw = smw.getParam(i);
                if (matchParam(arg, spw, pw)) {
                    newArgs[index] = arg;
                    continue loopPw;
                }

            }
            //注入attr里的值
            if (Utils.isEmpty(attr)){
                newArgs[index] = null;
                continue loopPw;
            }


            Object value = null;
            List<NameAndValue> nameAndValues = attr.get(type);
            if (Utils.isEmpty(nameAndValues)){

                if (nameAndValues.size() == 1){
                    value = nameAndValues.get(0).getValue();
                }else {
                    for (NameAndValue nameAndValue : nameAndValues) {
                        if (name.equals(nameAndValue.getName())){
                            value = nameAndValue.getValue();
                            break;
                        }
                    }
                }
            }

            newArgs[index] = value;
        }
        return newArgs;
    }




    private static boolean matchParam(Object arg, ParameterWrapper spw, ParameterWrapper pw){
        String spwName = spw.getName();
        String pwName = pw.getName();
        if (arg == null){
            return spwName.equals(pwName);
        }
        Class<?> argClass = arg.getClass();
        Class<?> spwType = spw.getType();
        Class<?> pwType = pw.getType();
        if (pwType.isAssignableFrom(spwType)){
            return true;
        }

        //如果父参数是代理参数 父类
        //则有可能无法转换的风险
        if (spwType.isAssignableFrom(pwType)){
            return pwType.isAssignableFrom(argClass);
        }

        return false;
    }

}
