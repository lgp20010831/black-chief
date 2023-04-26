package com.black.datasource;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.tools.BeanUtil;
import com.black.sql_v2.action.AbstractSqlOptServlet;

import javax.sql.DataSource;

public class SqlControllerElementResolver implements ProduceElementResolver{
    @Override
    public DataSource tellApart(Object element) {
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(element);
        if (AbstractSqlOptServlet.class.isAssignableFrom(primordialClass)){
            AbstractSqlOptServlet optServlet = (AbstractSqlOptServlet) InstanceBeanManager.instance(primordialClass, InstanceType.BEAN_FACTORY);
            String alias = optServlet.getAlias();
            return ConnectionManagement.getDataSource(alias);
        }
        return null;
    }
}
