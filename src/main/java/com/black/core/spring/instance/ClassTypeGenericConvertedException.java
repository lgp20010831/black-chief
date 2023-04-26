package com.black.core.spring.instance;

public class ClassTypeGenericConvertedException extends RuntimeException{

    Class<?> willConvertClassType;

    Class<?> paramClassType;

    public ClassTypeGenericConvertedException(Class<?> willConvertClassType, Class<?> paramClassType) {
        super("无法将类型:" + paramClassType +"转换成: " + willConvertClassType);
        this.willConvertClassType = willConvertClassType;
        this.paramClassType = paramClassType;
    }

    public Class<?> getParamClassType() {
        return paramClassType;
    }

    public Class<?> getWillConvertClassType() {
        return willConvertClassType;
    }
}
