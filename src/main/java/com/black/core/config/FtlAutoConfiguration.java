package com.black.core.config;

import com.black.core.util.Utils;
import com.black.ftl.FtlResolver;
import com.black.ftl.ResolveModel;
import com.black.spring.ChiefSpringHodler;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-05-15 9:54
 */

@SuppressWarnings("all")
@Data
@ConfigurationProperties(prefix = "ftl")
public class FtlAutoConfiguration implements InitializingBean {

    private List<String> paths = new ArrayList<>();

    private String nullDefaultValue = "";

    private ResolveModel resolveModel = ResolveModel.XML;

    @Override
    public void afterPropertiesSet() throws Exception {
        FtlResolver resolver = new FtlResolver();
        resolver.setNullDefaultValue(nullDefaultValue);
        resolver.setResolveModel(resolveModel);
        if (!Utils.isEmpty(paths)){
            resolver.loadPackages(paths.toArray(new String[0]));
        }
        DefaultListableBeanFactory factory = ChiefSpringHodler.getChiefAgencyListableBeanFactory();
        factory.registerSingleton("ftlResolver", resolver);
    }


}
