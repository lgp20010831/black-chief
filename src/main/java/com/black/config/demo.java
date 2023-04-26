package com.black.config;

import com.black.config.annotation.*;
import com.black.core.spring.util.ApplicationUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class demo {


    public static void main(String[] args) {
        ApplicationUtil.programRunMills(() -> {
            SpringApplicationConfigAutoInjector injector = new SpringApplicationConfigAutoInjector();
            //Pro bean = new Pro();
            Yml bean = new Yml();
            injector.pourintoBean(bean);
            System.out.println(bean);
        });

    }

    @Getter @Setter
    static class Yml{
        @Unnecessary
        @MatchAttribute({"**.datasource.**.datasource"})
        Map<String, DataPro> datasource;

        @Unnecessary
        @MatchAttribute("spring.**.datasource")
        DataPro dataPro;
    }

    @Getter @Setter
    @AttributePrefix("spring.datasource.dynamic")
    static class Pro{

        String primary;

        @MatchAttribute("**.datasource")
        Map<String, DataPro> datasource;

        @Attribute("datasource")
        List<DataPro> pros;

        @Attribute("datasource.one")
        DataPro pro;

        @Attribute("datasource.one")
        Map<String, String> onePros;

    }

    @Getter @Setter
    public static class DataPro{

        String driverClassName;

        String url;

        String username;

        String password;
    }
}
