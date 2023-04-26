package com.black.core.sql.code.pattern;

import com.black.core.query.ClassWrapper;
import com.black.core.sql.annotation.Appearance;
import com.black.core.sql.annotation.ImportPlatform;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.config.AppearanceConfiguration;
import com.black.core.sql.code.config.ConfigurationTreatment;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import com.black.table.TableMetadata;
import com.black.table.TableUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class AppearanceFactory {



    public static List<AppearanceConfiguration> parse(Appearance[] appearances, Configuration configuration, Connection connection){
        List<AppearanceConfiguration> result = new ArrayList<>();
        for (Appearance appearance : appearances) {
            String tableName = appearance.tableName();
            String[] names = tableName.split("/");
            for (String name : names) {
                TableMetadata metadata = TableUtils.getTableMetadata(name, connection);
                AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration(metadata,
                        StringUtils.hasText(appearance.foreignKeyName()) ? appearance.foreignKeyName() : metadata.firstForeignKey().getName(),
                        configuration);
                AnnotationUtils.loadAttribute(appearance, appearanceConfiguration);
                ClassWrapper<?> wrapper = appearanceConfiguration.getCw();
                if(wrapper.inlayAnnotation(ImportPlatform.class)){
                    wrapper = ClassWrapper.get(wrapper.getMergeAnnotation(ImportPlatform.class).value());
                }
                result.add((AppearanceConfiguration) ConfigurationTreatment.treatmentConfig(appearanceConfiguration, wrapper));
            }
        }
        return result;
    }


}
