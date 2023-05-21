package com.black.core.cache;

import com.black.core.convert.DefaultTypeContributor;
import com.black.core.convert.TypeHandler;
import com.black.core.convert.v2.TypeEngine;
import com.black.core.convert.v2.TypeUtilsBringRearTypeHandler;
import com.black.core.factory.manager.FactoryManager;
import com.black.utils.DefaultMappingKeyHandler;
import com.black.utils.TypeUtilsByConvert;

import java.util.Arrays;
import java.util.Collections;

public class TypeConvertCache {

    private static TypeHandler typeHandler;

    public static void registerTypeHandler(TypeHandler handler){
        typeHandler = handler;
    }

    public static TypeHandler getTypeHandler(){
        return typeHandler;
    }

    public static boolean existHandler(){
        return typeHandler != null;
    }

    public static void parseObj(Object... objs){
        TypeHandler typeHandler = initAndGet();
        typeHandler.parse(Arrays.asList(objs));
    }

    public static TypeHandler initAndGet(){
        TypeHandler handler = getTypeHandler();
        if (handler == null){
            handler = new TypeHandler(new DefaultMappingKeyHandler());
            FactoryManager.init();
            handler.parse(Collections.singleton(new DefaultTypeContributor(FactoryManager.getInstanceFactory())));
            registerTypeHandler(handler);
            //handler engine
            TypeEngine engine = TypeEngine.getInstance();
            engine.parseClass(TypeUtilsByConvert.class, true);
            engine.setBringRearTypeHandler(new TypeUtilsBringRearTypeHandler());
        }
        return handler;
    }
}
