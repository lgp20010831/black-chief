package com.black.utils;

import com.black.core.io.IoSerializer;
import com.black.core.io.ObjectSerializer;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class ReflexHandler {

    private static ObjectSerializer objectSerializer;

    public ReflexHandler(){

    }

    static {
        objectSerializer = new IoSerializer();
    }

    //-----------------------------------------------------------
    //                      get Fields
    //-----------------------------------------------------------



    public static Map<String, Field> nameWithAccessibleFields(Object obj)
    {
        return nameWithAccessibleFields(obj, false, Object.class);
    }

    public static Map<String, Field> nameWithAccessibleFields(Object obj, boolean andAssociationSuper)
    {
        return nameWithAccessibleFields(obj, andAssociationSuper, Object.class);
    }
    public static Map<String, Field> nameWithAccessibleFields(Object obj, Class<?> topSuper)
    {
        return nameWithAccessibleFields(obj, true, topSuper);
    }

    public static Map<String, Field> nameWithAccessibleFields(Object obj, boolean andAssociationSuper, Class<?> topSuper){
        List<Field> fields = getAccessibleFields(obj, andAssociationSuper, topSuper);
        HashMap<String, Field> map = new HashMap<>();
        for (Field field : fields) {
            map.put(field.getName(), field);
        }

        return map;
    }

    /** 默认不关联对象 */
    public static List<Field> getAccessibleFields(Object obj){
        return getAccessibleFields(obj, false, Object.class);
    }

    public static List<Field> getAccessibleFields(Class<?> obj) {
        return getAccessibleFields(obj, false, Object.class);
    }

    /** 默认顶级的管联父类 class 为 Object */
    public static List<Field> getAccessibleFields(Object obj, boolean andAssociationSuper){
        return getAccessibleFields(obj, andAssociationSuper, Object.class);
    }

    /** 访问的对象, 顶级 class 对象, 默认关联为 true */
    public static List<Field> getAccessibleFields(Object obj, Class<?> topSuper){
        return getAccessibleFields(obj, true, topSuper);
    }

    public static List<Field> getAccessibleFields(Object obj, boolean andAssociationSuper, Class<?> topSuper){
        return getAccessibleFields((Class<?>) (obj instanceof Class ? obj : obj.getClass()), andAssociationSuper, topSuper);
    }

    /***
     * 拿到可访问的字段, 返回一个集合,如果关联到其父类
     * @param objClass 要访问的对象
     * @param andAssociationSuper 是否关联其父类对象,访问其父类的属性
     * @param topSuper 如果关联其父类的属性, 则可以指定一个顶级的 class 对象,访问到该class后,停止访问,不包括 topSuper
     * @return 返回所有的字段
     */
    public static List<Field> getAccessibleFields(Class<?> objClass, boolean andAssociationSuper, Class<?> topSuper){

        //如果目标 class 和 topSuper 为空值
        if (objClass == null || topSuper == null)
            throw new NullPointerException("obj and topSuper should not be null");

        //如果目标 class 对象不继承于 topSuper
        if (!topSuper.isAssignableFrom(objClass))
             throw new RuntimeException(objClass + " should extends " + topSuper);

        //定义要操作的 class对象
        Class<?> targetClass = objClass;

        //定义一个集合来承载结果
        List<Field> fields = new ArrayList<>();

        for (;;){
            if (targetClass == null)
                break;

            //拿到  class 对象中的所有字段
            Field[] declaredFields = targetClass.getDeclaredFields();

            //遍历所有字段
            Arrays.stream(declaredFields).forEach(
                    f ->{

                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }
                        fields.add(f);
                    }
            );

            //如果不关联
            if (!andAssociationSuper)
                break;

            //如果到达的顶级 class
            if (topSuper.equals(targetClass.getSuperclass()))
                break;

            //切换操作的 class 对象为其父类 class
            targetClass = targetClass.getSuperclass();
        }

        return fields;
    }


    //-----------------------------------------------------------
    //                      get Methods
    //-----------------------------------------------------------
    /** 默认不关联对象 */
    public static List<Method> getAccessibleMethods(Object obj){
        return getAccessibleMethods(obj, false, Object.class);
    }

    public static List<Method> getAccessibleMethods(Class<?> objClass){
        return getAccessibleMethods(objClass, false, Object.class);
    }

    /** 默认顶级的管联父类 class 为 Object */
    public static List<Method> getAccessibleMethods(Object obj, boolean andAssociationSuper){
        return getAccessibleMethods(obj, andAssociationSuper, Object.class);
    }

    /** 访问的对象, 顶级 class 对象, 默认关联为 true */
    public static List<Method> getAccessibleMethods(Object obj, Class<?> topSuper){
        return getAccessibleMethods(obj, true, topSuper);
    }


    public static List<Method> getAccessibleMethods(Class<?> objClass, Class<?> topSuper){
        return getAccessibleMethods(objClass, true, topSuper);
    }

    public static List<Method> getAccessibleMethods(Object obj, boolean andAssociationSuper, Class<?> topSuper){

        return getAccessibleMethods((Class<?>) (obj instanceof Class<?> ? obj : obj.getClass()), andAssociationSuper, topSuper);
    }

    /***
     * 拿到可访问的方法, 返回一个集合,如果关联到其父类
     * @param objClass 要访问的对象
     * @param andAssociationSuper 是否关联其父类对象,访问其父类的属性
     * @param topSuper 如果关联其父类的属性, 则可以指定一个顶级的 class 对象,访问到该class后,停止访问,不包括 topSuper
     * @return 返回所有的方法
     */
    public static List<Method> getAccessibleMethods(Class<?> objClass, boolean andAssociationSuper, Class<?> topSuper){

        //如果目标 class 和 topSuper 为空值
        if (objClass == null || topSuper == null)
            throw new NullPointerException("obj and topSuper should not be null");

        //如果目标 class 对象不继承于 topSuper
        if (!topSuper.isAssignableFrom(objClass))
            throw new RuntimeException(objClass + " should extend " + topSuper);

        //定义要操作的 class对象
        Class<?> targetClass = objClass;

        //定义一个集合来承载结果
        List<Method> methods = new ArrayList<>();

        for (;;){

            if (targetClass == null){
                break;
            }

            //拿到  class 对象中的所有字段
            Method[] declaredMethods = targetClass.getDeclaredMethods();

            //遍历所有字段
            Arrays.stream(declaredMethods).forEach(
                    f ->{

                        if (!f.isAccessible()) {
                            f.setAccessible(true);
                        }
                        methods.add(f);
                    }
            );

            //如果不关联
            if (!andAssociationSuper)
                break;

            //如果到达的顶级 class
            if (topSuper.equals(targetClass.getSuperclass()))
                break;

            //切换操作的 class 对象为其父类 class
            targetClass = targetClass.getSuperclass();
        }

        return methods;
    }

    //-------------------------------------------------------------
    //                      get interfaces
    //-------------------------------------------------------------

    public interface InterfaceFilter {

        /***
         * 过滤接口,匹配的条件,返回true表示通过
         * @param interfaceClass 将要遍历传递的接口参数
         * @return 返回是否过滤掉
         */
        Boolean filter(Class<?> interfaceClass);
    }

    public static List<Class<?>> getInterfaces(InterfaceFilter filter, Class<?> objClass){

            return Arrays.stream(objClass.getInterfaces()).filter(
                    inter -> filter == null || filter.filter(inter)
            ).collect(Collectors.toList());
    }

    public static List<Class<?>> getInterfaces(Class<?> objClass)
    {
        return getInterfaces(null, objClass);
    }

    public static List<Class<?>> getInterfaces(Object obj)
    {
        return getInterfaces(null, obj.getClass());
    }

    public static List<Class<?>> getInterfaces(InterfaceFilter filter, Object obj)
    {
        return getInterfaces(filter, obj.getClass());
    }
    //------------------------------------------------------
    //                  Modifiers
    //------------------------------------------------------

    /** 修饰符类型 **/
    enum ReflexType{

        DEFAULT(0),
        PUBLIC(1),
        PRIVATE(2),
        PROTECTED(4),
        STATIC(8),
        FINAL(16);

        Integer code;

        ReflexType(Integer code) {
            this.code = code;
        }


        static ReflexType valueOf(Integer code){

            switch (code){

                case 0:
                    return ReflexType.DEFAULT;
                case 1:
                    return ReflexType.PUBLIC;
                case 2:
                    return ReflexType.PRIVATE;
                case 4:
                    return ReflexType.PROTECTED;
                case 8:
                    return ReflexType.STATIC;
                case 16:
                    return ReflexType.FINAL;
                default:
                    return null;
            }
        }
    }

    /** 判断字段是什么类型的 */
    public static ReflexType fieldModifiers(Field field){

        int modifiers = -1;

        try {

            modifiers = field.getModifiers();
        }catch (SecurityException ex){

            field.setAccessible(true);
            fieldModifiers(field);
        }
        return ReflexType.valueOf(modifiers);
    }

    /** 判断方法是什么类型的 */
    public static ReflexType methodModifiers(Method method){

        int modifiers = -1;

        try {

            modifiers = method.getModifiers();
        }catch (SecurityException ex){

            method.setAccessible(true);
            methodModifiers(method);
        }
        return ReflexType.valueOf(modifiers);
    }

    public static <F, S extends F> S deepFuse(S son, F father, boolean keepOldValues){
        return fuse(son, objectSerializer.copyObject(father), keepOldValues);
    }

    public static <F, S extends F> S fuse(@NotNull S son, @NotNull F father, boolean keepOldValues){

        List<Field> fields = getAccessibleFields(father);
        Map<String, Field> stringFieldMap =
                nameWithAccessibleFields(son, true, father.getClass().getSuperclass());

        for (Field field : fields) {
            Object val;
            try {
                val = field.get(father);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            if (val == null)
                continue;

            String name = field.getName();
            final Field sonField = stringFieldMap.get(name);
            if (sonField == null)
                throw new RuntimeException("意外的预期, 无法找到子类中的字段: " + name);
            Object oldVal;
            try {
                oldVal = sonField.get(son);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            if (oldVal == null){
                setValue(sonField, son, val);
            }else {
                if (!keepOldValues){
                    setValue(sonField, son, val);
                }
            }
        }
        return son;
    }

    public static void setValue(Field field, Object obj, Object value){
        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /***
     * 获取指定类实现的某个接口上的泛型
     * @param targetClass 要检查的类
     * @param achieveInterface 指定该接口, 检查该类实现该接口的泛型
     * @return 返回泛型 class 对象
     */
    public static Class<?>[] genericVal(Class<?> targetClass, Class<?> achieveInterface){

        if (targetClass == null)
            return new Class[0];

            Type[] genericInterfaces = targetClass.getGenericInterfaces();

            if(achieveInterface != null && !achieveInterface.isInterface()){
                if (log.isWarnEnabled()){
                    log.warn("achieve interface must be interface");
                }
                return new Class[0];
            }

            for (Type genericInterface : genericInterfaces) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;

                if (achieveInterface != null && !(load(parameterizedType.getRawType())).equals(achieveInterface))
                    continue;

                return loads(parameterizedType.getActualTypeArguments());
            }


        return new Class[0];
    }

    public static Class<?>[] loopUpGenerics(Type genericType, Class<?> achieveInterface){
        if (genericType instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) genericType;

            if (achieveInterface != null && !(load(parameterizedType.getRawType())).equals(achieveInterface))
                return new Class[0];

            return loads(parameterizedType.getActualTypeArguments());
        }else if (genericType instanceof Class){
            return genericVal((Class<?>) genericType, achieveInterface);
        }
        return new Class[0];
    }

    public static Class<?>[] genericVal(Field field, Class<?> achieveInterface){

        if (field == null)
            return new Class[0];

        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) genericType;

            if (achieveInterface != null && !(load(parameterizedType.getRawType())).equals(achieveInterface))
                return new Class[0];

            return loads(parameterizedType.getActualTypeArguments());
        }else if (genericType instanceof Class){
            return genericVal((Class<?>) genericType, achieveInterface);
        }

        return new Class[0];
    }


    public static Class<?>[] getParamterGenericVal(Parameter parameter){
        Type parameterizedType = parameter.getParameterizedType();
        if (!(parameterizedType instanceof ParameterizedType))
        {
            if (log.isDebugEnabled()){
                log.debug("target not implemented generic paramter class");
            }
            return new Class[0];
        }
        return loads(((ParameterizedType)parameterizedType).getActualTypeArguments());
    }

    public static Class<?>[] getParamterGenericVal(Method method, int index){
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        if (genericParameterTypes.length == 0 || genericParameterTypes.length - 1 < index){
            return new Class[0];
        }
        Type targetGengricParamterType = genericParameterTypes[index];
        if (!(targetGengricParamterType instanceof ParameterizedType))
        {
            if (log.isDebugEnabled()){
                log.warn("target not implemented generic paramter class");
            }
            return new Class[0];
        }
        return loads(((ParameterizedType)targetGengricParamterType).getActualTypeArguments());
    }

    public static Class<?>[] loopSuperGenericVal(Class<?> targetClass, int size){
        Class<?> type = targetClass;
        for (;;){
            Class<?>[] genericVal = superGenericVal(type);
            if (genericVal.length != size){
                Class<?> superclass = type.getSuperclass();
                if (superclass != null ){
                    type = type.getSuperclass();
                    continue;
                }else {
                    throw new IllegalStateException("error for extends AutoMapperController:" + type);
                }
            }
            return genericVal;
        }
    }

    /** 获取父类泛型 */
    public static Class<?>[] superGenericVal(Class<?> targetClass){

        if (targetClass == null)
            return new Class[0];

        Type genericSuperclass = targetClass.getGenericSuperclass();

        if (!(genericSuperclass instanceof ParameterizedType))
        {
            if (log.isDebugEnabled()){
                log.warn("target not implemented generic super class");
            }
            return new Class[0];
        }

        return loads(((ParameterizedType) genericSuperclass).getActualTypeArguments());
    }


    public static Class<?>[] loads(Type[] types){

        Class<?>[] classes = new Class<?>[types.length];

        for (int i = 0; i < types.length; i++) {

            try {
                String name = types[i].getTypeName();
                int s = name.indexOf("<");
                if (s != -1){
                    name = name.substring(0, s);
                }
                classes[i] = Class.forName(name);
            } catch (ClassNotFoundException e) {
                if (log.isErrorEnabled()){
                    log.error("can not load class {}", types[i]);
                }

                throw new RuntimeException("load class fail", e);
            }
        }
        return classes;
    }

    public static Class<?> load(@NotNull Type type){
        try {
            return Class.forName(type.getTypeName());
        } catch (ClassNotFoundException e) {
            if (log.isErrorEnabled()){
                log.error("load class fail {}", e.getMessage());
            }
            throw new RuntimeException("fail to load class:" + type, e);
        }
    }
}
