package com.black.core.sql.code.mapping;

import com.black.core.chain.*;
import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.IgnorePrint;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.spring.annotation.LoadSort;
import com.black.core.tools.BeanUtil;
import com.black.core.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;

@Log4j2
@IgnorePrint
@LoadSort(11)
@LazyLoading(EnabledGlobalMapping.class)
@ChainClient(GlobalMappingComponent.class)
public class GlobalMappingComponent implements OpenComponent, ChainPremise, CollectedCilent {

    public static final String SPLIT = "==>";

    public static final String CONFIG_PREFIX = "mapping";

    public static void loads(){
        GlobalMappingComponent component = new GlobalMappingComponent();
        component.load(null);
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
        Map<String, String> source = reader.getMasterAndSubApplicationConfigSource();
        for (String key : source.keySet()) {
            if (key.startsWith(CONFIG_PREFIX)){
                String k = key;
                k = k.substring(CONFIG_PREFIX.length());
                if (StringUtils.hasText(k)){
                    k = StringUtils.removeIfStartWith(k, ".");
                    GlobalMapping.registerMapping(k, source.get(key));
                }
            }
        }
        Map<String, String> mappingMap = GlobalMapping.getMappingMap();
        if (log.isInfoEnabled()) {
            log.info("global mapping existence: [{}]", mappingMap.size());
        }
    }

    @Override
    public boolean premise() {
        return ChiefApplicationRunner.isPertain(EnabledGlobalMapping.class);
    }

    @Override
    public void registerCondition(QueryConditionRegister register) {
        ConditionEntry entry = register.begin("annotation", hc -> {
            return hc.isAnnotationPresent(MappingProvider.class);
        });
        entry.instance(false);
        ConditionEntry inter = register.begin("inter", uhc -> {
            return ObtainMapping.class.isAssignableFrom(uhc) &&
                    BeanUtil.isSolidClass(uhc) &&
                    uhc.isAnnotationPresent(MappingProvider.class);
        });
    }

    @Override
    public void collectFinish(ConditionResultBody resultBody) {
        if ("annotation".equals(resultBody.getAlias())){
            for (Object cls : resultBody.getCollectSource()) {
                Class<?> clas = (Class<?>) cls;
                MappingProvider annotation = AnnotationUtils.getAnnotation(clas, MappingProvider.class);
                for (String txt : annotation.value()) {
                    parseAndRegister(txt);
                }
            }
        }

        if ("inter".equals(resultBody.getAlias())){
            for (Object o : resultBody.getCollectSource()) {
                ObtainMapping obtainMapping = (ObtainMapping) o;
                Map<String, String> mapping = obtainMapping.getMapping();
                if (mapping != null){
                    mapping.forEach(GlobalMapping::registerMapping);
                }
            }
        }
    }


    public void parseAndRegister(String txt){
        if (StringUtils.hasText(txt)){
            String[] split = StringUtils.split(txt, SPLIT, 2, "异常映射格式: " + txt);
            GlobalMapping.registerMapping(split[0].trim(), split[1].trim());
        }
    }
}
