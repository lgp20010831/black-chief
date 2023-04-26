package com.black.io.minio;

import com.black.core.util.Assert;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import io.minio.MinioClient;
import io.minio.messages.Bucket;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class MinioRawHandler {
    
    private final MinioClient client;

    private final String url;

    private String realUrl;

    private final String accessKey;

    private final String secretKey;

    private String defaultBucket;

    private final Map<String, Map<String, AbstractHierarchyMinioBucketHandler>> bucketMap = new ConcurrentHashMap<>();

    public MinioRawHandler(String ep, String ak, String sk){
        this(ep, ak, sk, null);
    }

    public MinioRawHandler(String ep, String ak, String sk, String defaultBucket) {
        try {

            url = ep;
            accessKey = ak;
            secretKey = sk;
            client = MinioClient.builder()
                    .endpoint(ep)
                    .credentials(ak, sk)
                    .build();
            if (StringUtils.hasText(defaultBucket)){
                this.defaultBucket = defaultBucket;
                createAndGetBucket(defaultBucket);
            }
        }catch (RuntimeException e){
            throw new MiniosException(e);
        }
    }

    public Set<String> bucketNameList(){
        try {
            return StreamUtils.mapSet(client.listBuckets(), Bucket::name);
        } catch (Throwable e) {
            throw new MiniosException(e);
        }
    }

    public boolean existBucket(String name){
        return bucketMap.containsKey(name);
    }

    public void setRealUrl(String realUrl) {
        this.realUrl = realUrl;
    }

    public String getRemote(){
        if (StringUtils.hasText(realUrl)){
            return realUrl;
        }
        return url;
    }

    public BucketHandler createAndGetBucket(String name){
        return createAndGetBucket(name, "");
    }

    public BucketHandler createAndGetBucket(String name, String levelName){
        Assert.notNull(name, "bucket is null");
        Map<String, AbstractHierarchyMinioBucketHandler> handlerMap = bucketMap.computeIfAbsent(name, bn -> {
            Map<String, AbstractHierarchyMinioBucketHandler> map = new HashMap<>();
            map.put("", new DefaultHierarchyBucketHandler(bn, this, ""));
            return map;
        });
        BucketHandler handler = handlerMap.get("");
        if (StringUtils.hasText(levelName)){
            handler = handlerMap.computeIfAbsent(name, nm -> {
                return new DefaultHierarchyBucketHandler(nm, this, levelName);
            });
        }
        return handler;
    }
}
