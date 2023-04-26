package com.black.aviator;

import com.black.aviator.function.NewListFunction;
import com.black.aviator.function.NewMapFunction;
import com.black.core.json.ReflexUtils;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.sql.code.condition.function.NotNullFunction;
import com.black.core.tools.BeanUtil;
import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.runtime.function.AbstractFunction;

import java.util.Map;
import java.util.Set;

public class AviatorManager {


    private static boolean init = false;

    public static boolean isEffective(){
        try {
            Class.forName("com.googlecode.aviator.AviatorEvaluator");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public static Object execute(String syntax){
        return execute(syntax, null);
    }

    public static Object execute(String syntax, Map<String, Object> env){
        return getInstance().execute(syntax, env);
    }

    public static AviatorEvaluatorInstance getInstance(){
        AviatorEvaluatorInstance instance = AviatorEvaluator.getInstance();
        if (!init){
            init = true;
            init(instance);
        }
        return instance;
    }

    private static void init(AviatorEvaluatorInstance instance){
        VfsScanner scanner = VFS.findVfsScanner();
        Set<Class<?>> classes = scanner.load("com.black.aviator.function");
        for (Class<?> type : classes) {
            if (BeanUtil.isSolidClass(type) && AbstractFunction.class.isAssignableFrom(type)){
                instance.addFunction((AbstractFunction)ReflexUtils.instance(type));
            }
        }
    }


    public static void main(String[] args) {
        getInstance().addFunction(new NotNullFunction());
        getInstance().addFunction(new NewMapFunction());
        getInstance().addFunction(new NewListFunction());
        ApplicationUtil.programRunMills(() ->{
            Object execute = getInstance().execute("ofMap('a', 1)");
            System.out.println(execute);
            System.out.println(getInstance().execute("asList(1, 2, 3)"));
        });

    }
}
