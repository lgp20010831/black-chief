package com.black.core.sql.code.config;

import com.black.core.sql.code.inter.DatabaseCompanyLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class BoundConfig {

    Set<String> excludeTable;

    Class<? extends DatabaseCompanyLevel> company;

    String[] sqlSequences;

    String suffix;

    Set<String> setValues;

    boolean relyAppearance;
}
