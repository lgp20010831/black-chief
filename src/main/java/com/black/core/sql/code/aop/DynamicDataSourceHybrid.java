package com.black.core.sql.code.aop;

import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.aop.code.HijackObject;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.DynamicArgurament;
import com.black.core.sql.annotation.OpenDynamicDataSourceArgurament;
import com.black.core.sql.code.datasource.CutDataSource;
import com.black.core.sql.code.datasource.Dynamics;
import com.black.core.util.StringUtils;
import com.black.core.util.Utils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;

@AopHybrid(SQLPremise.class)
@HybridSort(14501)
public class DynamicDataSourceHybrid implements AopTaskManagerHybrid, AopTaskIntercepet{

    private static final int METHOD_CUT = 0;

    private static final int ARG_CUT = 1;

    private static final int NO_CUT = -1;


    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent agent = AopMethodDirectAgent.getInstance();
        agent.register(this, (targetClazz, method) -> {
            if (AnnotationUtils.getAnnotation(method.getDeclaringClass(), CutDataSource.class) != null ||
                    AnnotationUtils.getAnnotation(method, CutDataSource.class) != null){
                return true;
            }

            return method.isAnnotationPresent(OpenDynamicDataSourceArgurament.class) ||
                    method.getDeclaringClass().isAnnotationPresent(OpenDynamicDataSourceArgurament.class);
        });
        return agent.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        return this;
    }

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        final MethodWrapper methodWrapper = MethodWrapper.get(hijack.getMethod());
        final Object[] args = hijack.getArgs();
        String dataSource;
        switch (getType(methodWrapper)){
            case METHOD_CUT:
                dataSource = processorMethodCut(methodWrapper, args);
                break;
            case ARG_CUT:
                dataSource = processorArgCut(methodWrapper, args);
                break;
            default:
                dataSource = null;
        }

        if (StringUtils.hasText(dataSource)){
            Dynamics.cutDataSource(dataSource);
        }
        try {
            return hijack.doRelease(hijack.getArgs());
        }finally {
            Dynamics.closeDataSource();
        }
    }

    int getType(MethodWrapper mw){
        ClassWrapper<?> cw = mw.getDeclaringClassWrapper();
        if (mw.hasAnnotation(CutDataSource.class) || cw.hasAnnotation(CutDataSource.class)){
            return METHOD_CUT;
        }

        if (mw.hasAnnotation(OpenDynamicDataSourceArgurament.class) || cw.hasAnnotation(OpenDynamicDataSourceArgurament.class)){
            return ARG_CUT;
        }
        return NO_CUT;
    }

    String processorMethodCut(MethodWrapper mw, Object[] args){
        CutDataSource annotation = getAnnotation(mw, CutDataSource.class);
        if (annotation != null){
            String value = annotation.value();
            return Utils.parseTopic(value);
        }
        return null;
    }

    String processorArgCut(MethodWrapper mw, Object[] args){
        OpenDynamicDataSourceArgurament annotation = getAnnotation(mw, OpenDynamicDataSourceArgurament.class);
        if (annotation != null){
            return getDataSourceByArgurament(annotation.value(), mw, args);
        }
        return null;
    }

    <T extends Annotation> T getAnnotation(MethodWrapper mw, Class<T> type){
        T annotation = mw.getAnnotation(type);
        if (annotation == null){
            return mw.getDeclaringClassWrapper().getAnnotation(type);
        }
        return null;
    }

    String getDataSourceByArgurament(String pointArgName, MethodWrapper mw, Object[] args){
        ParameterWrapper pw;
        String defaultValue = null;
        if (StringUtils.hasText(pointArgName)){
            pw = mw.getParameter(pointArgName);
        }else {
            pw = mw.getSingleParameterByAnnotation(DynamicArgurament.class);
            if (pw != null){
                defaultValue = pw.getAnnotation(DynamicArgurament.class).value();
            }
        }

        if (pw != null){
            Object arg = args[pw.getIndex()];
            if (arg == null){
                return defaultValue;
            }
            return arg.toString();
        }
        return null;
    }
}
