package com.black.core.aop.servlet.item;

import com.black.core.aop.servlet.item.annotation.DotHandler;
import com.black.core.chain.ChainClient;
import com.black.core.chain.CollectedCilent;
import com.black.core.chain.ConditionResultBody;
import com.black.core.chain.QueryConditionRegister;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StreamUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Collection;
import java.util.HashSet;

@ChainClient
public class DotHandlerHolder  implements CollectedCilent {

    private static final Collection<DotVariousHandler> variousHandlers = new HashSet<>();

    public static Collection<DotVariousHandler> getVariousHandlers() {
        return variousHandlers;
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        register.begin("dot", jut -> DotVariousHandler.class.isAssignableFrom(jut) &&
                BeanUtil.isSolidClass(jut) && AnnotationUtils.getAnnotation(jut, DotHandler.class) != null);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("dot".equals(resultBody.getAlias())){
            variousHandlers.addAll( StreamUtils.mapList(resultBody.getCollectSource(), rb -> (DotVariousHandler) rb));
        }
    }
}
