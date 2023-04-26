package com.black.core.entry;

import lombok.NonNull;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemIntermediateResolvers {

    public ItemParams parseItem(@NonNull String item, @NonNull Map<String, Object> source){
        int start = item.indexOf("(");
        if (start == -1){
            throw new RuntimeException("item is not vaild, lost start (");
        }
        int end = item.indexOf(")");
        if (end == -1){
            throw new RuntimeException("item is not vaild, lost end )");
        }

        String realItem = com.black.core.util.StringUtils.linkStr(item.substring(0, start +1), item.substring(end));
        String variables = item.substring(start + 1, end);
        if (!StringUtils.hasText(variables)){
            return new ItemParams(realItem, new Object[0]);
        }
        String[] variable = variables.split(",");
        List<Object> variableList = new ArrayList<>();
        for (String var : variable) {
            if (StringUtils.hasText(var)){
                variableList.add(source.get(var));
            }
        }
        return new ItemParams(realItem, variableList.toArray());
    }

}
