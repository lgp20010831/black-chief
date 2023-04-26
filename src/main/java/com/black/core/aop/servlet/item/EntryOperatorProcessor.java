package com.black.core.aop.servlet.item;

import com.black.core.aop.servlet.item.annotation.OperatorProcessor;
import com.black.core.cache.EntryCache;
import com.black.core.entry.EntryExtenderDispatcher;
import com.black.core.util.StringUtils;

import java.util.Map;

@OperatorProcessor
public class EntryOperatorProcessor extends AbstractLAOperatorProcessor{
    public EntryOperatorProcessor() {
        super('(', ')');
    }

    @Override
    public Object parse(Map<String, Object> globalVariable, Object previousValue, EachParagraphOperator paragraphOperator) throws Throwable {
        EntryExtenderDispatcher dispatcher = EntryCache.getDispatcher();
        if (dispatcher != null){
            String entry = StringUtils.linkStr(paragraphOperator.getFrontQuantity(), "(", paragraphOperator.getMediumQuantity(), ")");
            return dispatcher.handlerByMap(entry, globalVariable);
        }
        return null;
    }
}
