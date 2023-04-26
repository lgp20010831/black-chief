package com.black.file;

import com.alibaba.fastjson.JSONObject;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.util.Body;
import com.black.core.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractFileController {

    protected final FileHandler handler;

    protected IoLog log;

    protected AbstractFileController() {
        handler = new FileHandler();
        log = LogFactory.getLog4j();
    }

    public void setLog(IoLog log) {
        this.log = log;
    }

    protected boolean beforeUpload(UploadArgWrapper uploadArgWrapper){
        return false;
    }

    public Object upload(JSONObject body, Map<String, Object> queryParams, MultipartFile file){
        UploadArgWrapper argWrapper = new UploadArgWrapper();
        argWrapper.setArg(body);
        argWrapper.setFile(file);
        argWrapper.setQueryParams(queryParams);
        if (beforeUpload(argWrapper)) {
            log.debug("intercept upload file");
            return null;
        }
        Object result = handler.doUpload(argWrapper.getArg(), argWrapper.getFile(), argWrapper.getQueryParams());
        return afterUpload(argWrapper, result);
    }

    protected Object afterUpload(UploadArgWrapper uploadArgWrapper, Object result){
        return result;
    }

    protected boolean beforeUploadBatchWithJson(UploadBatchJSONArgWrapper argWrapper){
        return false;
    }

    public Object uploadBatchWithJson(JSONObject body, Map<String, Object> queryParams, List<MultipartFile> files) {
        UploadBatchJSONArgWrapper argWrapper = new UploadBatchJSONArgWrapper();
        argWrapper.setArg(body);
        argWrapper.setFiles(files);
        argWrapper.setQueryParams(queryParams);
        if (beforeUploadBatchWithJson(argWrapper)) {
            log.debug("intercept upload batch files with arg json");
            return null;
        }
        List<Object> result = StreamUtils.mapList(argWrapper.getFiles(),
                file -> handler.doUpload(argWrapper.getArg(), file, argWrapper.getQueryParams()));
        return afterUploadBatchWithJson(argWrapper, result);
    }

    protected Object afterUploadBatchWithJson(UploadBatchJSONArgWrapper argWrapper, List<Object> result){
        return result;
    }

    protected boolean beforeUploadBatchWithArray(UploadBatchArrayArgWrapper argWrapper){
        return false;
    }

    public Object uploadBatchWithArray(List<Map<String, Object>> jsonArray,
                                       Map<String, Object> queryParams, List<MultipartFile> files) {
        UploadBatchArrayArgWrapper argWrapper = new UploadBatchArrayArgWrapper();
        argWrapper.setArg(jsonArray);
        argWrapper.setFiles(files);
        argWrapper.setQueryParams(queryParams);
        if (beforeUploadBatchWithArray(argWrapper)) {
            log.debug("intercept upload batch files with arg array");
            return null;
        }
        List<Map<String, Object>> tempArray = argWrapper.getArg();
        List<MultipartFile> fileList = argWrapper.getFiles();
        List<Object> rbs = new ArrayList<>();
        for (int i = 0; i < fileList.size(); i++) {
            JSONObject json = null;
            if (tempArray != null && i < tempArray.size()){
                json = new Body(tempArray.get(i));
            }
            MultipartFile file = fileList.get(i);
            rbs.add(handler.doUpload(json, file, argWrapper.getQueryParams()));
        }
        return afterUploadBatchWithArray(argWrapper, rbs);
    }

    protected Object afterUploadBatchWithArray(UploadBatchArrayArgWrapper argWrapper, List<Object> rbs){
        return rbs;
    }

    protected boolean beforeDownload(FileEntity entity){
        return false;
    }

    public void download(FileEntity entity, HttpServletResponse response) throws IOException {
        if (beforeDownload(entity)){
            log.debug("intercept download file");
            return;
        }
        handler.doDownload(entity, response);
        afterDownload(entity, response);
    }

    protected void afterDownload(FileEntity entity, HttpServletResponse response){}

    protected boolean beforeDelete(FileEntity entity){
        return false;
    }

    public Object delete(FileEntity entity){
        if (beforeDelete(entity)){
            log.debug("intercept download file");
            return null;
        }
        return afterDelete(entity, handler.doDelete(entity));
    }

    protected Object afterDelete(FileEntity entity, Object result){
        return result;
    }


}
