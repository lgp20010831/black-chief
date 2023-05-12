package com.black.datasource;

import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.sql.code.SpringDataSourceBuilder;
import com.black.core.sql.code.YmlDataSourceBuilder;
import com.black.spring.ChiefSpringHodler;
import com.black.table.AbstractDataSourceBuilder;

import javax.sql.DataSource;

/**
 * @author 李桂鹏
 * @create 2023-05-12 9:43
 */
@SuppressWarnings("all")
public class IntelligentRecognitionDataSourceBuilder extends AbstractDataSourceBuilder {
    @Override
    protected DataSource createDataSource() {
        if (ChiefSpringHodler.getChiefAgencyListableBeanFactory() != null){
            //is spring env
            return new SpringDataSourceBuilder().getDataSource();
        }

        ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
        if (reader.selectAttribute("spring.datasource.dynamic.primary") != null) {
            //is mybatis dynamic env
            return new MybatisPlusDynamicDataSourceBuilder().createDataSource();
        }


        return new YmlDataSourceBuilder().getDataSource();
    }
}
