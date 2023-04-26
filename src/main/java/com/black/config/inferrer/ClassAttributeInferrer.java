package com.black.config.inferrer;

import com.black.config.ConfiguringAttributeAutoinjector;
import com.black.core.query.ClassWrapper;

import java.util.Map;

public interface ClassAttributeInferrer {

    boolean support(ClassWrapper<?> cw);

    Map<String, String> infer(ClassWrapper<?> cw, Object bean, ConfiguringAttributeAutoinjector autoinjector);
}
