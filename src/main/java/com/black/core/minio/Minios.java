package com.black.core.minio;

import com.black.io.minio.BucketHandler;
import com.black.io.minio.MinioRawHandler;
import com.black.core.util.Assert;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Minios {

    public static final String DEFAULT = "default";

    private static final Map<String, MinioRawHandler> rawMap = new ConcurrentHashMap<>();

    public static boolean containClient(String alias){
        return rawMap.containsKey(alias);
    }

    public static void registerClient(@NonNull MinioRawHandler handler, @NonNull String alias){
        if (rawMap.containsKey(alias)){
            throw new IllegalStateException("client: [" + alias + "] is already exists");
        }
        rawMap.put(alias, handler);
    }

    public static MinioRawHandler getClient(String alias){
        return rawMap.get(alias);
    }

    public static BucketHandler def(){
        return def(null, "");
    }

    public static BucketHandler def(String bucket){
        return def(bucket, "");
    }

    public static BucketHandler def(String bucket, String file){
        return get(DEFAULT, bucket, file);
    }

    public static BucketHandler get(String alias){
        return get(alias, null, "");
    }

    public static BucketHandler get(String alias, String bucket){
        return get(alias, bucket, "");
    }

    public static BucketHandler get(String alias, String bucket, String file){
        MinioRawHandler handler = getClient(alias);
        Assert.notNull(handler, "client: [" + alias + "] is non-existent");
        return handler.createAndGetBucket(bucket == null ? handler.getDefaultBucket() : bucket, file);
    }
}
