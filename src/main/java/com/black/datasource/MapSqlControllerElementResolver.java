package com.black.datasource;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.sql.action.DynamicController;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.tools.BeanUtil;

import javax.sql.DataSource;

public class MapSqlControllerElementResolver implements ProduceElementResolver{
    @Override
    public DataSource tellApart(Object element) {
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(element);
        if (DynamicController.class.isAssignableFrom(primordialClass)){
            DynamicController dynamicController = (DynamicController) InstanceBeanManager.instance(primordialClass, InstanceType.BEAN_FACTORY);
            GlobalParentMapping mapper = dynamicController.getMapper();
            String alias = mapper.getAlias();
            return ConnectionManagement.getDataSource(alias);
        }
        return null;
    }
}
