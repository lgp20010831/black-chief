package com.black.core.entry;


import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.utils.ReflexHandler;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntryParser {

    public static final String END_FLAG = ")";
    public static final String START_FLAG = "(";
    public static final String CONN = "-";
    public static final String METHOD_FLAG = "()";

    protected ItemConfiguration doParseMethod(Object obj, Class<?> primordialClass,
                                              Method method, String prefix, String item){
        if(prefix != null){
            item = StringUtils.linkStr(prefix, CONN, item);
        }
        if (!item.endsWith(METHOD_FLAG)){
            item = StringUtils.linkStr(item, METHOD_FLAG);
        }
        return new ItemConfiguration(primordialClass, method, item, obj);
    }

    public Map<String, ItemConfiguration> doParse(Collection<Object> donors){
        Map<String, ItemConfiguration> result = new ConcurrentHashMap<>();
        for (Object donor : donors) {
            String prefix = null;
            Class<Object> primordialClass = BeanUtil.getPrimordialClass(donor);
            ItemDonor itemDonor = AnnotationUtils.getAnnotation(primordialClass, ItemDonor.class);
            String donorValue = itemDonor.value();
            if (StringUtils.hasText(donorValue)){
                prefix = donorValue;
            }

            boolean includeAllMethods = itemDonor.includeAllMethods();
            for (Method method : ReflexHandler.getAccessibleMethods(primordialClass)) {
                ItemConfiguration configuration;
                if (includeAllMethods){
                    configuration = doParseMethod(donor, primordialClass, method, prefix, method.getName());
                }else {
                    ItemMapping mapping = AnnotationUtils.getAnnotation(method, ItemMapping.class);
                    if (mapping == null){
                        continue;
                    }
                    configuration = doParseMethod(donor, primordialClass, method, prefix, mapping.value());
                }
                String item = configuration.getItem();
                if (result.containsKey(item)){
                    throw new RuntimeException("item : " + item + " 已经存在");
                }else {
                    result.put(item, configuration);
                }
            }
        }
        return result;
    }

}
