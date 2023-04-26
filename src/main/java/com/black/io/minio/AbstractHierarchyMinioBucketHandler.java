package com.black.io.minio;

import com.black.core.util.StringUtils;
import io.minio.ListObjectsArgs;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayInputStream;

@Log4j2
public abstract class AbstractHierarchyMinioBucketHandler extends AbstractMinioBucketHandler{

    //子文件夹名称
    private final String levelName;

    protected AbstractHierarchyMinioBucketHandler(String bucket,
                                                  @NonNull MinioRawHandler handler,
                                                  String levelName) {
        super(bucket, handler);
        this.levelName = levelName;
        if(StringUtils.hasText(levelName)){
            checkAndCreateLevel();
        }
    }

    public AbstractHierarchyMinioBucketHandler checkAndCreateLevel(){
        try {

            Iterable<Result<Item>> iterable = getClient().listObjects(
                    ListObjectsArgs.builder()
                            .bucket(getBucket())
                            .prefix(levelName)
                            .recursive(true)
                            .build());
            if (iterable != null && iterable.iterator().hasNext())
                return this;
            if(log.isInfoEnabled()){
                log.info("桶内文件夹: {}, 不存在,立即创建", levelName);
            }

            getClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(getBucket())
                            .object(levelName.concat(FILE_SEGMENTATION))
                            .stream(new ByteArrayInputStream(new byte[]{}), 0 ,-1)
                            .build()
            );
            return this;
        }catch (Throwable e){
            throw new MiniosException(e);
        }
    }

    public String getLevelName() {
        return levelName;
    }

    @Override
    String getFileObjectName(String rawName) {
        return StringUtils.hasText(levelName) ? levelName + FILE_SEGMENTATION + rawName : rawName;
    }
}
