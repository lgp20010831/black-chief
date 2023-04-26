package com.black.aviator;

import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.ConstructorWrapper;
import com.black.core.query.FieldWrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//环境类
public class ObjectEnv {

    //存贮的构造参数传递的参数
    private final Map<String, Object> createParam = new HashMap<>();

    private final Map<String, Object> fieldParam = new HashMap<>();

    public void parseCreateParams(ConstructorWrapper<?> cw, Object[] args){
        for (ParameterWrapper pw : cw.getParameterWrappers()) {
            createParam.put(pw.getName(), args[pw.getIndex()]);
        }
    }

    public void parseFieldParams(Object bean, ClassWrapper<?> cw){
        Collection<FieldWrapper> fields = cw.getFields();
        for (FieldWrapper field : fields) {
            fieldParam.put(field.getName(), field.getValue(bean));
        }
    }


    public Map<String, Object> getCreateParam() {
        return createParam;
    }

    public Map<String, Object> getFieldParam() {
        return fieldParam;
    }
}
