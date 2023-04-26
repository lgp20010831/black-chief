package com.black.core.entry;

import com.black.core.cache.EntryCache;
import com.black.core.chain.*;
import com.black.core.tools.BeanUtil;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.List;
import java.util.Map;

@ChainClient
public class EntryExtenderDispatcher implements CollectedCilent {

    public static final String ALIAS = "ENTRY_HANDLER";

    private Map<String, ItemConfiguration> cache;

    private final ItemIntermediateResolvers intermediateResolvers;

    public EntryExtenderDispatcher(){
        EntryCache.setDispatcher(this);
        intermediateResolvers = new ItemIntermediateResolvers();
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry conditionEntry = register.begin(ALIAS,
                h -> BeanUtil.isSolidClass(h) && AnnotationUtils.getAnnotation(h, ItemDonor.class) != null);
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if (resultBody.getAlias().equals(ALIAS)) {
            EntryParser parser = new EntryParser();
            cache = parser.doParse(resultBody.getCollectSource());
        }
    }

    public List<String> queryParamList(String entry){
        ItemConfiguration configuration = cache.get(entry);
        if (configuration != null){
            return configuration.getParamterNameList();
        }
        return null;
    }

    public Object handlerByArgs(String entry, Object... args){
        if (cache == null){
            return null;
        }
        ItemConfiguration configuration = cache.get(entry.trim());
        if (configuration == null){
            return null;
        }
        return configuration.invoke(args);
    }

    public Object handlerByMap(String entry, Map<String, Object> source){
        if (cache == null){
            return null;
        }
        ItemParams itemParams = intermediateResolvers.parseItem(entry, source);
        ItemConfiguration configuration = cache.get(itemParams.getItem().trim());
        if (configuration == null){
            return null;
        }
        return configuration.invoke(itemParams.getArgs());
    }
}
