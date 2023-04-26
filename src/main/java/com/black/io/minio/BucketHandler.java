package com.black.io.minio;

import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.DeleteError;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface BucketHandler {

    String getBucket();

    MinioClient getClient();

    MinioRawHandler getHandler();

    boolean exist();

    void create();

    MinioBody upload(@NonNull MultipartFile mf);

    MinioBody upload(@NonNull File file);

    MinioBody upload(byte[] buf, String name, String contentType);

    MinioBody upload(InputStream in, String name, String contentType);

    List<String> fileNameList();

    File getFile(String name, String toPath);

    InputStream getFile(String name);

    List<InputStream> fileInList();

    void download(String name, String path);

    String getFileUrl(String name);

    boolean hasFile(String name);

    void removeFile(String name);

    void removeFile(List<String> names);

    Iterable<Result<DeleteError>> removeFiles(Set<String> names);
}
