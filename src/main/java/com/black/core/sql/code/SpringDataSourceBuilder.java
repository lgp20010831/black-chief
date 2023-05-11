package com.black.core.sql.code;

import com.black.datasource.SpringDataSourceWrapper;
import com.black.holder.SpringHodler;
import com.black.core.factory.beans.MainConstructor;
import com.black.core.util.Assert;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import javax.sql.DataSource;

public class SpringDataSourceBuilder implements DataSourceBuilder {

    private DefaultListableBeanFactory beanFactory;

    public SpringDataSourceBuilder(){
        this(null);
    }

    @MainConstructor
    public SpringDataSourceBuilder(DefaultListableBeanFactory beanFactory){
        this.beanFactory = beanFactory;
    }

    @Override
    public DataSource getDataSource() {
        if(beanFactory == null){
            beanFactory = (DefaultListableBeanFactory) SpringHodler.getBeanFactory();
        }
        Assert.notNull(beanFactory, "can not find bean factory");
        DataSource dataSource = beanFactory.getBean(DataSource.class);
        return new SpringDataSourceWrapper(dataSource);
    }
}
