package com.black.core.convert;

import com.black.core.cache.TypeConvertCache;
import com.black.core.query.ClassWrapper;

public class ConvertUtils {



    public static Object[] checkMethodArgs(Object[] args, Class<?>[] parameterTypes){
        if (args != null){
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                Object arg = args[i];
                if (arg != null){

                    Class<?> argClass = arg.getClass();
                    if (!ClassWrapper.autoAssemblyAndDisassembly(parameterType, argClass)) {
                        TypeHandler handler = TypeConvertCache.initAndGet();
                        arg = handler.convert(parameterType, arg);
                        args[i] = arg;
                    }
                }
            }
        }
        return args;
    }
}
