package com.black.core.sql.code.config;

import com.black.core.query.MethodWrapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter @Getter
public class BlendUpdateOrAddConfig extends Configuration{

    private Set<String> whenAppearanceSingleMapping;

    private Set<String> whenSelectCondition;

    public BlendUpdateOrAddConfig(GlobalSQLConfiguration configuration, MethodWrapper mw) {
        super(configuration, mw);
    }


}
