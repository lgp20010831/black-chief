package com.black.file;

import com.alibaba.fastjson.JSONObject;
import com.black.core.mvc.response.ResponseUtil;
import com.black.core.util.Body;
import com.black.core.util.StreamUtils;
import com.black.throwable.IOSException;
import com.black.utils.IoUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileHandler {

    protected static Map<FileType, FileSaver<Object>> saverMap = new ConcurrentHashMap<>();

    public static void putSaver(FileType type, FileSaver<Object> saver){
        saverMap.put(type, saver);
    }

    protected BrahmaFileImpl brahmaFileImpl;

    private FileType fileType = FileType.COMMON;

    public FileHandler(){
        //init
        brahmaFileImpl = new BrahmaFileImpl();
        putSaver(FileType.COMMON, new CommonFileSaver());
        putSaver(FileType.MINIO, new DefaultMinioFileSaver());
    }

    public FileType getType(){
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public Object upload(JSONObject body, Map<String, Object> queryParams, MultipartFile file){
        return doUpload(body, file, queryParams);
    }


    public Object uploadBatchWithJson(JSONObject body, Map<String, Object> queryParams, List<MultipartFile> files) {
        return StreamUtils.mapList(files, file -> doUpload(body, file, queryParams));
    }


    public Object uploadBatchWithArray(List<Map<String, Object>> jsonArray, Map<String, Object> queryParams, List<MultipartFile> files) {
        List<Object> rbs = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            JSONObject json = null;
            if (jsonArray != null && i < jsonArray.size()){
                json = new Body(jsonArray.get(i));
            }
            MultipartFile file = files.get(i);
            rbs.add(doUpload(json, file, queryParams));
        }
        return rbs;
    }

    public void download(FileEntity entity, HttpServletResponse response) throws IOException {
        doDownload(entity, response);
    }

    public Object delete(FileEntity entity){
        return doDelete(entity);
    }

    protected Object doDelete(FileEntity entity){
        boolean delete = brahmaFileImpl.delete(entity, getType());
        if (delete) {
            whenDelete(entity);
        }
        return delete;
    }

    protected void doDownload(FileEntity entity, HttpServletResponse response) throws IOException {
        InputStream in = brahmaFileImpl.getFileInputStream(entity, getType());
        ServletOutputStream out = response.getOutputStream();
        ResponseUtil.configResponse(response, entity.getFileName());
        IoUtils.writeIn(in, out);
    }


    protected Object doUpload(JSONObject body, MultipartFile file, Map<String, Object> queryParams){
        try {
            final FileType type = getType();
            AfterHandlerFileBody handlerFileBody = brahmaFileImpl.doCommonUpload(body, file, type, queryParams);
            Object result = handlerFileBody.getSaveResult();
            Object preResult = callBackByParams(handlerFileBody.getPartBody(),
                    handlerFileBody.getQueryBody(),
                    handlerFileBody.getSaveResult(),
                    handlerFileBody.getType(),
                    handlerFileBody.getEntity(),
                    handlerFileBody.getMultipartFile());
            if (preResult != null){
                result = preResult;
            }

            Object callBack = callBack(handlerFileBody);
            if (callBack != null){
                result = callBack;
            }

            return result;
        }catch (Throwable e){
            throw new IOSException(e);
        }
    }

    //子类重写
    protected void whenDelete(FileEntity entity){

    }

    //子类重写
    protected Object callBack(AfterHandlerFileBody fileBody){
        return null;
    }

    //子类重写
    protected Object callBackByParams(JSONObject partBody,
                                      Body queryBody,
                                      Object saveResult,
                                      FileType type,
                                      FileEntity entity,
                                      MultipartFile multipartFile){
        return null;
    }




}
