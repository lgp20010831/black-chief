package com.black.core.sql.code.impl.appearance_impl;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.factory.beans.WriedBean;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.ImportPlatform;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.BoundConfig;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.ConfigurationTreatment;
import com.black.core.sql.code.ill.StopSqlInvokeException;
import com.black.core.sql.code.inter.DatabaseCompanyLevel;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import com.black.table.TableMetadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractBoundAppearanceResolver extends AbstractAppearanceResolver{

    @WriedBean
    BeanFactory beanFactory;

    protected final ThreadLocal<Set<String>> boundTableNames = new ThreadLocal<>();

    @Override
    protected List<AppearanceConfiguration> parse(MethodWrapper mw, Configuration configuration, Connection connection) {
        BoundConfig boundConfig = loadConfig(mw, configuration, connection);
        Class<? extends DatabaseCompanyLevel> company = boundConfig.getCompany();
        DatabaseCompanyLevel companyLevel = beanFactory.getSingleBean(company);
        List<AppearanceConfiguration> configurationList = new ArrayList<>();
        try {

            List<TableMetadata> companyLevelTables = companyLevel.getCompanyLevelTable(configuration, connection);
            for (TableMetadata levelTable : companyLevelTables) {
                if (boundConfig.getExcludeTable().contains(levelTable.getTableName())){
                    continue;
                }
                AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration(levelTable,
                        levelTable.getForeignByPrimaryName(configuration.getPrimaryName()).getName(),
                        configuration);
                BeanUtil.mappingBean(boundConfig, appearanceConfiguration);
                String name = configuration.convertAlias(appearanceConfiguration.getTableName());
                name = StringUtils.appendIfNotEmpty(name, boundConfig.getSuffix());
                appearanceConfiguration.setAppearanceName(name);
                ClassWrapper<?> wrapper = appearanceConfiguration.getCw();
                if(wrapper.inlayAnnotation(ImportPlatform.class)){
                    wrapper = ClassWrapper.get(wrapper.getMergeAnnotation(ImportPlatform.class).value());
                }
                configurationList.add((AppearanceConfiguration) ConfigurationTreatment.treatmentConfig(appearanceConfiguration, wrapper));
            }
        } catch (SQLException e) {
            throw new StopSqlInvokeException("中止原因无法获取该主表下所有关联表信息: " + e.getMessage());
        }
        return configurationList;
    }

    protected abstract BoundConfig loadConfig(MethodWrapper mw, Configuration configuration, Connection connection);
}
