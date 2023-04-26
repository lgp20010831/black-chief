package com.black.project;

import com.black.template.Configuration;

import java.util.Map;

public interface VersionConfigurer {


    void postConfiguration(Configuration configuration,
                           Map<String, Object> source,
                           ChiefProjectGenerator projectGenerator);



}
