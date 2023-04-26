package com.black.io.minio;

import java.io.IOException;

public class MINIODOME {


    public static void main(String[] args) throws IOException {
        MinioRawHandler handler = new MinioRawHandler("http://10.20.255.225:9000", "ldb", "LDB@2021");
        System.out.println(handler.bucketNameList());
        BucketHandler bucketHandler = handler.createAndGetBucket("srm");
        System.out.println(bucketHandler.fileNameList());
    }

}
