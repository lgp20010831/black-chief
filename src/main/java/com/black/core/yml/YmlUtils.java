package com.black.core.yml;

import lombok.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@SuppressWarnings("all")
public class YmlUtils {


    public static Map<String, Object> castYmlToMap(@NonNull InputStream inputStream){
        Yaml yaml = new Yaml();
        return yaml.loadAs(inputStream, Map.class);
    }


}
