package com.black.config.inferrer;

import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.config.annotation.AttributePrefix;
import com.black.mq_v2.MqttUtils;
import com.black.core.query.ClassWrapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigurerAttributePrefixInferrer implements ClassAttributeInferrer {
    @Override
    public boolean support(ClassWrapper<?> cw) {
        return MqttUtils.findAnnotation(cw.get(), AttributePrefix.class) != null;
    }

    @Override
    public Map<String, String> infer(ClassWrapper<?> cw, Object bean, ConfiguringAttributeAutoinjector autoinjector) {
        AttributePrefix annotation = MqttUtils.findAnnotation(cw.get(), AttributePrefix.class);
        String[] values = annotation.value();
        Map<String, String> result = new LinkedHashMap<>();
        if (values.length == 0){
            return autoinjector.getDataSource();
        }

        for (String value : values) {
            Map<String, String> attributes = autoinjector.selectGroupSources(value, true);
            result.putAll(attributes);
        }
        return result;
    }
}
