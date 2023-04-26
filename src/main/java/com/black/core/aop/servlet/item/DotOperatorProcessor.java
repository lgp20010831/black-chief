package com.black.core.aop.servlet.item;

import com.black.core.aop.servlet.item.annotation.OperatorProcessor;
import com.black.core.json.ReflexUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

@OperatorProcessor
public class DotOperatorProcessor extends AbstractLAOperatorProcessor{

    private Collection<DotVariousHandler> variousHandlers;

    public static final String METHOD_INVOKE_FLAG = "#";

    public DotOperatorProcessor() {
        super('.');
    }

    @Override
    public Object parse(Map<String, Object> globalVariable, Object previousValue, EachParagraphOperator paragraphOperator) throws Throwable {
        String frontQuantity = paragraphOperator.getFrontQuantity();
        if (!StringUtils.hasText(frontQuantity) && previousValue == null){
            return null;
        }
        Object parseObject = previousValue == null ? globalVariable.get(frontQuantity) : previousValue;
        if (parseObject == null){
            return null;
        }
        return doParse(parseObject, globalVariable, paragraphOperator);
    }

    protected Object doParse(Object parseObject, Map<String, Object> globalVariable, EachParagraphOperator paragraphOperator){
        if (variousHandlers == null){
            variousHandlers = DotHandlerHolder.getVariousHandlers();
        }

        if(variousHandlers == null){
            return null;
        }

        for (DotVariousHandler variousHandler : variousHandlers) {
            if (variousHandler.support(parseObject)) {
                return variousHandler.handler(parseObject, globalVariable, paragraphOperator);
            }
        }
        return null;
    }

    public static Object parseMethod(Object parseObject, Map<String, Object> globalVariable, String item){
        if (item.contains(METHOD_INVOKE_FLAG)){
            int i = item.indexOf(DotOperatorProcessor.METHOD_INVOKE_FLAG);
            String methodName = item.substring(0, i);
            String paramNames = item.substring(i + 1);
            int count = 0;
            String[] paramSplitArray = new String[0];
            if(StringUtils.hasText(paramNames)){
                count = (paramSplitArray = paramNames.split(",")).length;
            }
            Method method = ReflexUtils.getMethod(methodName, count, parseObject);
            if (method != null){
                boolean voidValue = method.getReturnType().equals(Void.class);
                Object[] args = new Object[paramSplitArray.length];
                for (int y = 0; y < paramSplitArray.length; y++) {
                    String name = paramSplitArray[y];
                    if (name.startsWith("'")) {
                        if (!name.endsWith("'")){
                            throw new RuntimeException("缺少常量变量结束标志: '");
                        }

                        args[y] = name.substring(1, name.length() - 1);
                    }else {
                        args[y] = globalVariable.get(name);
                    }
                }
                Object val = ReflexUtils.invokeMethod(method, parseObject, args);
                return voidValue ? parseObject : val;
            }
        }
        return null;
    }


}
