package com.black.core.sql.code.config;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.*;
import com.black.core.sql.code.parse.BlendObject;
import com.black.core.sql.code.parse.CharParser;
import com.black.core.util.Vfu;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ConfigurationTreatment {

    private static Map<String, Map<String, BlendObject>> cache = new ConcurrentHashMap<>();

    public static Configuration treatmentConfig(Configuration configuration){
        return treatmentConfig(configuration, configuration.getCw());
    }

    public static Configuration treatmentConfig(Configuration configuration, ClassWrapper<?> cw){
        MethodWrapper mw = configuration.getMethodWrapper();
        if (mw != null && cw != null){
            processorCurrency(cw, configuration, mw);
        }
        return configuration;
    }

    private static void processorCurrency(ClassWrapper<?> cw, Configuration configuration, MethodWrapper mw){
        if (cw.hasAnnotation(CurrencySequences.class) && !mw.hasAnnotation(UnsupportCurrency.class)) {
            processorSeq(configuration, cw.getAnnotation(CurrencySequences.class).value());
        }

        if (cw.hasAnnotation(CurrencySetValues.class) && !mw.hasAnnotation(UnsupportCurrency.class)) {
            processorSetValues(configuration, cw.getAnnotation(CurrencySetValues.class).value());
        }

        if (cw.hasAnnotation(GlobalPlatform.class) && !mw.hasAnnotation(UnsupportPlatform.class)){
            processorPlatform(configuration, cw.getAnnotation(GlobalPlatform.class).value(), list -> {
                joinSeq(configuration, list);
            });
        }

        if (cw.hasAnnotation(GlobalSetPlatform.class) && !mw.hasAnnotation(UnsupportPlatform.class)){
            processorPlatform(configuration, cw.getAnnotation(GlobalSetPlatform.class).value(), list -> {
                joinSet(configuration, list);
            });
        }
    }

    private static String[] getPlatformName(Configuration configuration, MethodWrapper mw){
        String[] alias = {};
        ProvidePlatform annotation = mw.getAnnotation(ProvidePlatform.class);
        if (annotation != null){
            alias = annotation.value();
            return alias;
        }
        switch (configuration.getMethodType()){
            case INSERT:
                return new String[]{"insert"};
            case UPDATE:
                return new String[]{"update"};
            case DELETE:
                return new String[]{"delete"};
            case QUERY:
                return new String[]{"select"};
        }
        return alias;
    }

    private static void processorPlatform(Configuration configuration,
                                          String txt,
                                          Consumer<List<String>> consumer){
        Map<String, BlendObject> blendObjectMap = cache.computeIfAbsent(txt, ty -> {
            List<BlendObject> blendObjects = CharParser.parseBlend(txt);
            return CharParser.toMap(blendObjects);
        });

        String[] platformNames = getPlatformName(configuration, configuration.getMethodWrapper());
        for (String platformName : platformNames) {
            BlendObject object = blendObjectMap.get(platformName);
            if (object != null){
                consumer.accept(object.getAttributes());
            }
        }
    }

    private static void joinSeq(Configuration configuration, List<String> seqs){
        Set<String> sqlSequences = configuration.getSqlSequences();
        if (sqlSequences != null){
            sqlSequences.addAll(seqs);
        }
    }

    private static void joinSet(Configuration configuration, List<String> seqs){
        Set<String> setValues = configuration.getSetValues();
        if (setValues != null){
            setValues.addAll(seqs);
        }
    }

    private static void processorSeq(Configuration configuration, String[] cs){
        Set<String> sqlSequences = configuration.getSqlSequences();
        if (cs.length > 0 && sqlSequences != null){
            Set<String> css = Vfu.set(cs);
            sqlSequences.addAll(css);
        }
    }

    private static void processorSetValues(Configuration configuration, String[] cs){
        Set<String> setValues = configuration.getSetValues();
        if (cs.length > 0 && setValues != null){
            setValues.addAll(Arrays.asList(cs));
        }
    }
}
