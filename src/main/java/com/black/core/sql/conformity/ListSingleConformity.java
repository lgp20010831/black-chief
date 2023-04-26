package com.black.core.sql.conformity;

import com.black.core.json.ReflexUtils;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.MasterResult;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.OrderlyMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ListSingleConformity implements ConformityPolicy{

    @Override
    public boolean support(OrderlyMap<Configuration, Object> map) {
        Configuration configuration = map.firstKey();
        MethodWrapper mw = configuration.getMethodWrapper();
        Class<?> returnType = mw.getReturnType();
        Class<?>[] gv;
        return map.isSingle() &&
                (configuration.getMethodType() == SQLMethodType.QUERY ||
                        configuration.getMethodType() == SQLMethodType.UPDATE ||
                        configuration.getMethodType() == SQLMethodType.INSERT) &&
                (!Map.class.isAssignableFrom(returnType) &&
                        (Collection.class.isAssignableFrom(returnType) &&
                                (gv = ReflexUtils.getMethodReturnGenericVals(mw.getMethod())).length == 1
                                && !Map.class.isAssignableFrom(gv[0])));
    }

    @Override
    public Object doConformity(OrderlyMap<Configuration, Object> map, OrderlyMap<Configuration, Object> queue) {
        Configuration configuration = map.firstKey();
        Object element = map.firstElement();
        boolean single = !List.class.isAssignableFrom(configuration.getMethodWrapper().getReturnType());
        if (single){
            return element;
        }
        Collection<Object> collection;
        if (!(element instanceof Collection)){
            collection = SQLUtils.wrapperList(element);
        }else {
            collection = (Collection<Object>) element;
        }
        if (!configuration.getMethodWrapper().hasAnnotation(MasterResult.class)){
            queue.forEach((apc, r) ->{
                if (r instanceof Collection){
                    collection.addAll((Collection<Object>) r);
                }else {
                    collection.add(r);
                }
            });
        }
        return collection;
    }
}
