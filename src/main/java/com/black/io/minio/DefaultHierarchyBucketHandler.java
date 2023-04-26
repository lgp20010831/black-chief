package com.black.io.minio;

import lombok.NonNull;

public class DefaultHierarchyBucketHandler extends AbstractHierarchyMinioBucketHandler{

    public DefaultHierarchyBucketHandler(String bucket,
                                         @NonNull MinioRawHandler handler,
                                         String levelName
                                         ) {
        super(bucket, handler, levelName);
    }


}
