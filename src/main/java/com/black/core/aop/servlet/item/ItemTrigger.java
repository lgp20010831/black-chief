package com.black.core.aop.servlet.item;

import com.black.core.cache.EntryCache;
import com.black.core.entry.EntryExtenderDispatcher;
import com.black.core.util.Av0;
import com.black.core.util.Body;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//作为条目触发器
public class ItemTrigger {

    private final Map<String, Object> paramterSource;

    private final ThreadLocalItemResolver itemResolver;

    public ItemTrigger(Map<String, Object> paramterSource, ThreadLocalItemResolver itemResolver) {
        this.paramterSource = paramterSource;
        this.itemResolver = itemResolver;
    }

    public Object invoke(String item){
        return invoke(item, new HashMap<>());
    }

    public Object invoke(String item, Map<String, Object> indoorSource){
        EntryExtenderDispatcher dispatcher = EntryCache.getDispatcher();
        if (dispatcher != null){
            List<String> paramList = dispatcher.queryParamList(item);
            if (paramList == null){
                throw new RuntimeException("无法找到执行条目: " + item);
            }

            //准备参数
            Object[] args = new Object[paramList.size()];
            Map<String, Object> mergeSource = mergeSource(indoorSource);
            for (int i = 0; i < paramList.size(); i++) {
                args[i] = mergeSource.get(paramList.get(i));
            }
            return dispatcher.handlerByArgs(item, args);
        }
        return null;
    }

    protected Map<String, Object> mergeSource(Map<String, Object> indoorSource){
        Body body = Av0.bw(paramterSource);
        if (indoorSource != null){
            body.putAll(indoorSource);
        }
        return body;
    }

    public Object execute(String... laExpressions){
        return execute(new HashMap<>(), laExpressions);
    }

    public Object execute(Map<String, Object> variables, String... laExpressions){
        return execute(variables, true, laExpressions);
    }

    /***
     * zhiixng la 表达式
     * @param variables 变量, 优先级高于参数变量
     * @param useParamterVariables 是否使用参数变量, 默认 true 开启
     * @param laExpressions 表达式
     * @return 返回执行的结果
     */
    public Object execute(Map<String, Object> variables, boolean useParamterVariables, String... laExpressions){
        Map<String, Object> fv = new HashMap<>();
        if (useParamterVariables){
            fv.putAll(paramterSource);
        }
        fv.putAll(variables);
        Object result = null;
        for (String laExpression : laExpressions) {
            if (StringUtils.hasText(laExpression)){
                result = itemResolver.resolver(fv, laExpression);
            }
        }
        return result;
    }
}
