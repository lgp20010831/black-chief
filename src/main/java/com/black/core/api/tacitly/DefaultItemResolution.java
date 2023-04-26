package com.black.core.api.tacitly;

import com.black.core.api.handler.ItemResolutionModule;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

public class DefaultItemResolution extends AbstractItemResolution{

    public DefaultItemResolution(String name) {
        super(name);
    }

    @Override
    public ItemResolutionModule parse(String item, Class<?> superClass, ApiDependencyManger dependencyManger) {
        Map<Class<?>, List<Class<?>>> dependencyMap = dependencyManger.getDependencyMap();
        ApiAliasManger aliasManger = dependencyManger.getAliasManger();

        if (!StringUtils.hasText(item)){
            List<Class<?>> classes = dependencyMap.get(superClass);
            dependencyClasses.addAll(classes);
            return this;
        }

        String[] sameLevel = item.split(",");
        for (String same : sameLevel) {
            if (same.contains("{")){
                Map<String, String> parseLevelItem = parseLevelItem(same);
                parseLevelItem.forEach((field, it) ->{
                    lowLevel.add(new DefaultItemResolution(field).parse(item, superClass, dependencyManger));
                });
            }else {
                //当此条目没有包含下级标识符
                //则表示此字段代表实体类或者普通字段
                Class<?> queryPojo = aliasManger.queryPojo(same);
                if (queryPojo == null){
                    dependencyFields.add(same);
                }else {
                    dependencyClasses.add(queryPojo);
                }
            }
        }
        return this;
    }
}
