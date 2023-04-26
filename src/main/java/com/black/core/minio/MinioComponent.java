package com.black.core.minio;

import com.black.io.minio.MinioRawHandler;
import com.black.core.config.ApplicationConfigurationReader;
import com.black.core.config.ApplicationConfigurationReaderHolder;
import com.black.core.ill.GlobalThrowableCentralizedHandling;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;
import com.black.core.util.StringUtils;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@LazyLoading(EnabledMinios.class)
public class MinioComponent implements OpenComponent {

    public static final String CONFIG_PREFIX = "minios";
    public static final String CONFIG_URL = "minios.default.url";
    public static final String CONFIG_ACCESS_KEY = "minios.default.accessKey";
    public static final String CONFIG_SECRET_KEY = "minios.default.secretKey";
    public static final String CONFIG_BUCKET = "minios.default.bucket";
    public static final String CONFIG_REAL_URL = "minios.default.realUrl";
    public static final String CONFIG_FILE = "minios.default.file";
    private final Map<String, Map<String, String>> configMap = new HashMap<>();

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        try {
            ApplicationConfigurationReader reader = ApplicationConfigurationReaderHolder.getReader();
            Map<String, String> source = reader.getMasterAndSubApplicationConfigSource();
            for (String key : source.keySet()) {
                if (key.startsWith(CONFIG_PREFIX)){
                    String[] configInfo = StringUtils.split(key, "\\.", 3, "ill config info: " + key);
                    Map<String, String> map = configMap.computeIfAbsent(configInfo[1], ci -> new HashMap<>());
                    map.put(configInfo[2], source.get(key));
                }
            }

            configMap.forEach((alias, cf) ->{
                MinioRawHandler handler = new MinioRawHandler(cf.get("url"), cf.get("accessKey"), cf.get("secretKey"), cf.get("bucket"));
                Minios.registerClient(handler, alias);
                handler.createAndGetBucket(cf.get("bucket"), cf.get("file"));
                handler.setRealUrl(cf.get("realUrl"));
            });
            log.info("current minio client size is [{}]", configMap.size());
        }catch (Throwable e){
            GlobalThrowableCentralizedHandling.resolveThrowable(e);
        }
    }
}
