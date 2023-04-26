package com.black.io.minio;

import com.black.core.mvc.FileUtil;
import com.black.core.util.Av0;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Log4j2
public abstract class AbstractMinioBucketHandler implements BucketHandler{

    private final String bucket;

    private final MinioClient client;

    private final MinioRawHandler handler;

    public static final String FILE_SEGMENTATION = "/";

    protected AbstractMinioBucketHandler(String bucket, @NonNull MinioRawHandler handler) {
        this.client = handler.getClient();
        this.handler = handler;
        if (!StringUtils.hasText(bucket)){
            throw new IllegalArgumentException("bucket");
        }
        this.bucket = bucket;
        if (!exist()) {
            create();
        }
    }

    @Override
    public MinioClient getClient() {
        return client;
    }

    @Override
    public MinioRawHandler getHandler() {
        return handler;
    }

    public String getBucket() {
        return bucket;
    }

    abstract String getFileObjectName(String rawName);

    public boolean exist(){
        try {
            return client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build());
        } catch (Throwable e) {
            throw new MiniosException(e);
        }
    }

    public void create(){
        try {
            client.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucket)
                            .build());
        } catch (Throwable e) {
            throw new MiniosException(e);
        }
    }

    public MinioBody upload(@NonNull MultipartFile mf){
        try {
            return upload(mf.getInputStream(), mf.getOriginalFilename(), mf.getContentType());
        } catch (IOException e) {
            throw new MiniosException(e);
        }
    }

    public MinioBody upload(@NonNull File file){
        if (!file.exists()){
            throw new MiniosException("file is not exist");
        }
        if (!file.canRead()) {
            throw new MiniosException("file is can not read");
        }

        try {
            return upload(new FileInputStream(file), file.getName(), null);
        } catch (FileNotFoundException e) {
            throw new MiniosException(e);
        }
    }

    public MinioBody upload(byte[] buf, String name, String contentType){
        return upload(new ByteArrayInputStream(buf), name, contentType);
    }

    public MinioBody upload(InputStream in, String name, String contentType){
        try {
            if (!StringUtils.hasText(contentType)){
                contentType = "application/octet-stream";
            }
            ObjectWriteResponse writeResponse = client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(getFileObjectName(name))
                            .stream(in, in.available(), -1)
                            .contentType(contentType)
                            .build()
            );
            MinioBody body = new MinioBody();
            body.setObjectName(writeResponse.object());
            body.setUrl(StringUtils.linkStr(handler.getRemote(), FILE_SEGMENTATION, getBucket(), FILE_SEGMENTATION, writeResponse.object()));
            if (log.isInfoEnabled()) {
                log.info("upload file [{}]", body.getObjectName());
            }
            return body;
        }catch (Throwable e){
            throw new MiniosException(e);
        }
    }

    public List<String> fileNameList(){
        try {
            List<String> nameList = new ArrayList<>();
            Iterable<Result<Item>> iterable = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .recursive(true)
                            .build()
            );
            for (Result<Item> itemResult : iterable) {
                nameList.add(itemResult.get().objectName());
            }
            return nameList;
        }catch (Throwable e){
            throw new MiniosException(e);
        }
    }

    public File getFile(String name, String toPath){
        InputStream in = getFile(name);
        File f = FileUtil.createFile(toPath);
        if (f == null) {
            throw new MiniosException("file invaild");
        }
        try (FileOutputStream out = new FileOutputStream(f)){
            for (int i = 0; i < in.available(); i++) {
                out.write(i);
            }
            out.flush();
            return f;
        }catch (Throwable e){
            throw new MiniosException(e);
        }
    }

    public InputStream getFile(String name){
        try {
            return client.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(name)
                            .build()
            );
        }catch (Throwable e){
            throw new MiniosException(e);
        }
    }

    public List<InputStream> fileInList(){
        List<InputStream> ins = new ArrayList<>();
        try {
            for (String name : fileNameList()) {
                ins.add(getFile(name));
            }
            return ins;
        }catch (Throwable e){
            throw new MiniosException(e);
        }
    }

    public void download(String name, String path){
        try {
            client.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucket)
                            .object(name)
                            .filename(path)
                            .build());
        } catch (Throwable e) {
            throw new MiniosException(e);
        }
    }

    public String getFileUrl(String name){
        try {
           return client.getPresignedObjectUrl(
                            GetPresignedObjectUrlArgs.builder()
                                    .method(Method.GET)
                                    .bucket(bucket)
                                    .object(name)
                                    .expiry(2, TimeUnit.HOURS)
                                    .extraQueryParams(Av0.of("response-content-type", "application/json"))
                                    .build());
        } catch (Throwable e) {
            throw new MiniosException(e);
        }
    }

    public boolean hasFile(String name){
        try {
            Iterable<Result<Item>> iterable = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .recursive(true)
                            .build()
            );
            for (Result<Item> next : iterable) {
                if (next.get().objectName().equals(name))
                    return true;
            }
            return false;
        }catch (Throwable e){
            throw new MiniosException(e);
        }
    }

    public void removeFile(String name){
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(name).build());
        }catch (Throwable e){
            throw new MiniosException(e);
        }
    }

    @Override
    public void removeFile(List<String> names) {
        for (String name : names) {
            removeFile(name);
        }
    }

    //"Error in deleting object " + error.objectName() + "; " + error.message()
    public Iterable<Result<DeleteError>> removeFiles(Set<String> names){
        return client.removeObjects(RemoveObjectsArgs.builder().bucket(bucket)
                .objects(StreamUtils.mapList(names,DeleteObject::new)).build());
    }
}
