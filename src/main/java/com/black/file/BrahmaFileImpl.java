package com.black.file;

import com.alibaba.fastjson.JSONObject;
import com.black.core.mvc.FileUtil;
import com.black.core.util.Assert;
import com.black.utils.ServiceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class BrahmaFileImpl {

    public FileSaver<?> getSaver(FileType type){
        FileSaver<Object> saver = FileHandler.saverMap.get(type);
        Assert.notNull(saver, "unknown type :" + type + " of saver");
        return saver;
    }

    protected AfterHandlerFileBody doCommonUpload(JSONObject body, MultipartFile file, FileType type, Map<String, Object> queryParams) throws IOException {
        AfterHandlerFileBody handlerFileBody = new AfterHandlerFileBody(body, queryParams, type, file);
        FileEntity entity = getEntity(file);
        handlerFileBody.setEntity(entity);
        FileSaver<?> saver = getSaver(type);
        Object result = saver.saveFile(file.getInputStream(), entity);
        if (result instanceof FileEntityResultSetter){
            ((FileEntityResultSetter) result).setFileEntity(entity);
        }
        handlerFileBody.setSaveResult(result);
        return handlerFileBody;
    }

    public FileEntity getEntity(MultipartFile file){
        FileEntity entity = new FileEntity();
        entity.setFileName(file.getOriginalFilename());
        entity.setSize(file.getSize());
        entity.setUploadTime(ServiceUtils.now());
        entity.setType(FileUtil.getExpand(entity.getFileName()));
        return entity;
    }


    public InputStream getFileInputStream(FileEntity entity, FileType type){
        FileSaver<?> saver = getSaver(type);
        InputStream stream = saver.download(entity);
        Assert.notNull(stream, "unknown file entity:" + entity);
        return stream;
    }

    public boolean delete(FileEntity entity, FileType type){
        FileSaver<?> saver = getSaver(type);
        return saver.deleteFile(entity);
    }


}
