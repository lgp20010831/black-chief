package com.black.arg.original;

import com.black.arg.MethodReflectionIntoTheParameterProcessor;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Utils;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class CompatibleTypeHandler implements OriginalArgStrategyHandler {
    @Override
    public boolean support(OriginalArgStrategy strategy) {
        return strategy == OriginalArgStrategy.compatible_type;
    }

    @Override
    public Object handler(MethodWrapper mw, ParameterWrapper pw, Map<String, Object> originalArgMap,
                          MethodReflectionIntoTheParameterProcessor parameterProcessor) {
        if (Utils.isEmpty(originalArgMap)){
            return null;
        }
        Class<?> type = pw.getType();
        String targetName = pw.getName();
        if (parameterProcessor.isOriginArgTypeFirst()){
            List<String> matchTypes = new ArrayList<>();
            //如果是类型优先
            for (String key : originalArgMap.keySet()) {
                Object arg = originalArgMap.get(key);
                if (arg == null){
                    continue;
                }

                Class<Object> primordialClass = BeanUtil.getPrimordialClass(arg);
                if (type.isAssignableFrom(primordialClass)){
                    matchTypes.add(key);
                }
            }
            if (matchTypes.size() == 0){
                return null;
            }

            if (matchTypes.size() == 1){
                return originalArgMap.get(matchTypes.get(0));
            }

            if (matchTypes.contains(targetName)){
                return originalArgMap.get(targetName);
            }

            log.warn("Unable to obtain matching parameters, multiple matching types found: target missing: {}", targetName);
            return null;
        }else {
            return originalArgMap.get(targetName);
        }
    }


}
