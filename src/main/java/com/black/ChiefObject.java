package com.black;

import com.black.core.query.ClassWrapper;
import com.black.core.util.IoUtil;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

//作者: 李桂鹏, 时间: 2022-08-01 21:16:00
public interface ChiefObject extends Serializable {


    default ChiefObject parent(){
        return null;
    }

    String toString();

    default byte[] toBuffer() throws IOException {
        Class<? extends ChiefObject> self = getClass();
        Method toString;
        try {
             toString= self.getMethod("toString");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        Class<?> declaringClass = toString.getDeclaringClass();
        if (!declaringClass.equals(ChiefObject.class)){
            return toString().getBytes();
        }

        return IoUtil.toBuffer(this);
    }

    default ClassWrapper<? extends ChiefObject> classWrapper(){
        return ClassWrapper.get(getClass());
    }

    default int role(){
        return 0;
    }

    default int order(){
        return -1;
    }
}
