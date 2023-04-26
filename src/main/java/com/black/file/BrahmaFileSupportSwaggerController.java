package com.black.file;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public abstract class BrahmaFileSupportSwaggerController extends AbstractFileController {


    public BrahmaFileSupportSwaggerController(){
        FileType type = getType();
        handler.setFileType(type);
    }

    //获取操作文件的方式
    //分为传统将文件存到服务器地址里 和 将文件存入到 minio 服务器中
    protected FileType getType(){
        return FileType.COMMON;
    }

    @ApiOperation("上传单个文件")
    @PostMapping(path = "upload", consumes = "multipart/form-data", produces = "application/json")
    public Object upload(@RequestPart(value = "body", required = false) JSONObject body,
                         @RequestParam(required = false) Map<String, Object> queryParams,
                         @RequestPart("file") MultipartFile file){

        return super.upload(body, queryParams, file);
    }

    @ApiOperation("上传多个文件且支持json格式数据")
    @PostMapping(path = "uploadBatchWithJson", consumes = "multipart/form-data", produces = "application/json")
    public Object uploadBatchWithJson(@RequestPart(value = "body", required = false) JSONObject body,
                                      @RequestParam(required = false) Map<String, Object> queryParams,
                                      @RequestPart("file") List<MultipartFile> files) {
        return super.uploadBatchWithJson(body, queryParams, files);
    }

    @ApiOperation("上传多个文件且支持数组格式数据")
    @PostMapping(path = "uploadBatchWithArray", consumes = "multipart/form-data", produces = "application/json")
    public Object uploadBatchWithArray(@RequestPart(value = "array", required = false) List<Map<String, Object>> jsonArray,
                                       @RequestParam(required = false) Map<String, Object> queryParams,
                                       @RequestPart("file") List<MultipartFile> files) {
        return super.uploadBatchWithArray(jsonArray, queryParams, files);

    }

    @ApiOperation("下载文件")
    @PostMapping(path = "download", consumes = "application/json", produces = "application/octet-stream")
    public void download(@RequestBody FileEntity entity, HttpServletResponse response) throws IOException {
        super.download(entity, response);
    }

    @ApiOperation("删除文件")
    @PostMapping(path = "delete", consumes = "application/json", produces = "application/json")
    public Object delete(@RequestBody FileEntity entity){
        return super.delete(entity);
    }


}
