package com.black.core.native_sql;

import com.black.holder.SpringHodler;
import com.black.core.util.Assert;
import org.springframework.beans.factory.BeanFactory;

import javax.sql.DataSource;

public class SpringNativeBlendResolver implements NativeBlendSupportResolver{
    @Override
    public boolean support(String alias) {
        return "spring".equalsIgnoreCase(alias);
    }

    @Override
    public TransactionHandlerAndDataSourceHolder obtainDataSource(String value) {
        BeanFactory beanFactory = SpringHodler.getBeanFactory();
        Assert.notNull(beanFactory, "can not find spring beanfactory");
        DataSource dataSource;
        if ("default".equalsIgnoreCase(value)){
            dataSource = beanFactory.getBean(DataSource.class);
        }else {
            dataSource = beanFactory.getBean(value, DataSource.class);
        }
        return new TransactionHandlerAndDataSourceHolder(
                new SpringTransactionDataSourceHandlerWrapperDataSource(dataSource),
                dataSource,
                value + "-NativeMapper"
        );
    }
}
