package com.black.datasource;

import com.black.config.annotation.MatchAttribute;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DataSourceProperties {

    @MatchAttribute({
            "**.datasource.**.*url*",
            "**.datasource.**.*jdbcUrl*",
            "*url*",
            "*jdbcUrl"
    })
    private String url;

    @MatchAttribute({
            "**.datasource.**.*driverClassName*",
            "driverClassName"
    })
    private String driverClassName;

    @MatchAttribute({
            "**.datasource.**.*username*",
            "username"
    })
    private String username;

    @MatchAttribute({
            "**.datasource.**.*password*",
            "password"
    })
    private String password;

    @MatchAttribute("**.druid")
    private DruidConfig druidConfig;

    @MatchAttribute("**.hikari")
    private HikariCpConfig hikariCpConfig;

}
