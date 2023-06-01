package com.black.sql_v2.period;


import com.black.asm.Demo;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.factory.beans.AbstractBeanFactory;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.datasource.SpringConnectionWrapper;
import com.black.function.Function;
import com.black.spring.ChiefSpringHodler;
import com.black.table.ColumnMetadata;
import com.black.table.PrimaryKey;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;
import com.black.utils.CollectionUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.*;

/**
 * 用来获取 spring 环境中的 jdbc 成员变量
 * @author 李桂鹏
 * @create 2023-05-13 15:07
 */
@SuppressWarnings("all")
public class JDBCUtils {

    public static final String ALIAS = "JDBC_UTILS_$$";

    public static Connection getConnection(){
        DefaultListableBeanFactory factory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        DataSource dataSource = factory.getBean(DataSource.class);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        return new SpringConnectionWrapper(dataSource, connection);
    }


    public static <C> C borrow(Function<Connection, C> function){
        Connection connection = getConnection();
        try {
            return function.apply(connection);
        } catch (Throwable e) {
            throw new SQLSException(e);
        }finally {
            SQLUtils.closeConnection(connection);
        }
    }

    public static TableMetadata getTableMetadata(String tableName){
        return borrow(connection -> {
            return TableUtils.getTableMetadata(tableName, connection);
        });
    }

    public static Set<String> getTableNames(){
        return borrow(connection -> {
            return TableUtils.getCurrentTables(ALIAS, connection);
        });
    }

    public static List<TableMetadata> getTableMetadatas(){
        Set<String> tableNames = getTableNames();
        return StreamUtils.mapList(tableNames, JDBCUtils::getTableMetadata);
    }

    public static String getTableColumnDescribe(String tableName){
        TableMetadata metadata = getTableMetadata(tableName);
        return getTableColumnDescribe(metadata);
    }

    public static String getTableColumnDescribe(TableMetadata metadata){
        StringJoiner joiner = new StringJoiner(",", "(", ")");
        for (ColumnMetadata columnMetadata : metadata.getColumnMetadatas()) {
            String name = columnMetadata.getName();
            joiner.add(name);
        }
        return joiner.toString();
    }

    public static String getObjectDescribe(Object obj){
        return getObjectDescribe(obj, new HumpColumnConvertHandler());
    }

    public static String getObjectDescribe(Object obj, AliasColumnConvertHandler convertHandler){
        if (obj instanceof TableMetadata){
            return getTableColumnDescribe((TableMetadata) obj);
        }

        if (obj instanceof Map){
            return getDescribe(((Map<String, ?>) obj).keySet(), convertHandler);
        }

        if (obj instanceof Collection){
            if (((Collection<?>) obj).isEmpty()){
                return "empty collection";
            }

            Object element = CollectionUtils.firstElement((Collection<? extends Object>) obj);
            return getObjectDescribe(element, convertHandler);
        }

        ClassWrapper<?> wrapper = BeanUtil.getPrimordialClassWrapper(obj);
        return getDescribe(wrapper.getFieldNames(), convertHandler);
    }

    private static String getDescribe(Collection<String> attrs, AliasColumnConvertHandler convertHandler){
        StringJoiner joiner = new StringJoiner(",", "(", ")");
        for (String attr : attrs) {
            if (convertHandler != null){
                attr = convertHandler.convertColumn(attr);
            }
            joiner.add(attr);
        }
        return joiner.toString();
    }

    public static String getClassDescribe(Object obj){
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(obj);
        ClassWrapper<Object> classWrapper = ClassWrapper.get(primordialClass);
        String name = classWrapper.getName();
        int modifiers = primordialClass.getModifiers();
        Class<? super Object> superclass = primordialClass.getSuperclass();
        Class<?>[] interfaces = primordialClass.getInterfaces();
        List<String> list = StreamUtils.mapList(Arrays.asList(interfaces), Class::getName);
        String itfInfo = StringUtils.joinStringWithDel(",", list.toArray());
        StringJoiner fieldJoiner = new StringJoiner(",", "(", ")");
        for (FieldWrapper fieldWrapper : classWrapper.getFields()) {
            if (!fieldWrapper.get().getDeclaringClass().equals(primordialClass)) {
                continue;
            }
            String fn = fieldWrapper.getName();
            int fm = fieldWrapper.get().getModifiers();
            String typeName = fieldWrapper.getType().getName();
            String desc = StringUtils.letString(fm, "|", typeName, "|", fn);
            fieldJoiner.add(desc);
        }
        StringJoiner methodJoiner = new StringJoiner(",", "(", ")");
        for (MethodWrapper method : classWrapper.getMethods()) {
            if (!method.get().getDeclaringClass().equals(primordialClass)) {
                continue;
            }
            int mm = method.getMethod().getModifiers();
            String methodName = method.getName();
            String rn = method.getReturnType().getName();
            StringJoiner paramJoiner = new StringJoiner(",", "(", ")");
            for (ParameterWrapper parameterWrapper : method.getParameterArray()) {
                String pn = parameterWrapper.getType().getName();
                String parameterWrapperName = parameterWrapper.getName();
                paramJoiner.add(pn + "|" + parameterWrapperName);
            }
            String desc = StringUtils.letString(mm, "|", methodName, "|", paramJoiner.toString(), "|", rn);
            methodJoiner.add(desc);
        }
        Object[] infos = new Object[6];
        infos[0] = modifiers;
        infos[1] = name;
        infos[2] = superclass.getName();
        infos[3] = itfInfo;
        infos[4] = fieldJoiner.toString();
        infos[5] = methodJoiner.toString();
        StringJoiner classJoiner = new StringJoiner("&", "{", "}");
        for (Object info : infos) {
            classJoiner.add(String.valueOf(info));
        }
        return classJoiner.toString();
    }

    public static void main(String[] args) {
        String chiefFactory = getClassDescribe(AbstractBeanFactory.class);
        String dfFactory = getClassDescribe(DefaultListableBeanFactory.class);
        System.out.println(chiefFactory);
        System.out.println(chiefFactory.length());
        System.out.println(dfFactory);
        System.out.println(dfFactory.length());
    }
}
