package com.black.core.ill.aop;

import com.black.core.cache.EntryCache;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.entry.EntryExtenderDispatcher;
import com.black.core.util.CentralizedExceptionHandling;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalThrowableHandlerManagement {

    private static final ThreadLocal<IllSourceWrapper> runningIll = new ThreadLocal<>();

    private static final Collection<IllHandler> handlers = new HashSet<>();

    public static void registerHandler(IllHandler handler){
        if (handler != null){
            handlers.add(handler);
        }
    }

    public static void registerAll(Collection<IllHandler> handlers){
        if (handlers != null){
            GlobalThrowableHandlerManagement.handlers.addAll(handlers);
        }
    }

    public static Object handlerThrowable(IllSourceWrapper sourceWrapper) throws Throwable{
        IllSourceWrapper startIll = runningIll.get();
        if (startIll != null){
            sourceWrapper.setSourceWrapper(startIll);
        }

        ThrowableCaptureConfiguration configuration = sourceWrapper.getConfiguration();
        AtomicInteger handlerNum = new AtomicInteger(0);
        try {
            if (configuration.isWriteStackHeap()){
                CentralizedExceptionHandling.handlerException(sourceWrapper.getError());
            }
            for (IllHandler handler : handlers) {
                if (handler.support(sourceWrapper)) {
                    if (!handler.intercept(sourceWrapper)) {
                        return handler.handler(sourceWrapper);
                    }
                    handlerNum.incrementAndGet();
                }
            }
            if (handlerNum.get() == 0 && configuration.isIfUnattendedCapture()){

                /**
                 * Read the temporary results on the configuration annotation,
                 * and then perform type processing
                 */
                String captureResult = configuration.getCaptureResult();
                Object result = captureResult;
                if (!StringUtils.hasText(captureResult)){
                    return null;
                }

                //get item performer
                EntryExtenderDispatcher dispatcher = EntryCache.getDispatcher();

                //Gets the processor of the type conversion
                TypeHandler typeHandler = TypeConvertCache.initAndGet();

                if (isQualifiedItem(captureResult)) {
                    if (dispatcher != null){
                        result = dispatcher.handlerByArgs(captureResult);
                    }
                }else {

                    Class<?> resultType = configuration.getResultType();
                    if (typeHandler != null){
                        result = typeHandler.convert(resultType, captureResult);
                    }
                }

                /**
                 * Check whether the result matches the return type of the method.
                 * If not, convert it
                 */
                Class<?> returnType = sourceWrapper.getMethodWrapper().getReturnType();
                if (result != null){
                    if (!returnType.isAssignableFrom(result.getClass())){
                        if (typeHandler != null){
                            result = typeHandler.convert(returnType, result);
                        }
                    }
                }
                return result;
            }
            throw sourceWrapper.getError();
        }finally {
            runningIll.set(sourceWrapper);
        }
    }

    protected static boolean isQualifiedItem(String item){
        return item.contains("(") && item.contains(")");
    }

    public static void close(){
        runningIll.remove();
    }
}
