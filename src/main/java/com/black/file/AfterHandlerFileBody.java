package com.black.file;

import com.alibaba.fastjson.JSONObject;
import com.black.core.util.Body;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Getter @Setter
public class AfterHandlerFileBody {

    private JSONObject partBody;

    private Body queryBody;

    private Object saveResult;

    private FileType type;

    private FileEntity entity;

    private MultipartFile multipartFile;

    public AfterHandlerFileBody(JSONObject partBody, Map<String, Object> queryBody, FileType type, MultipartFile multipartFile) {
        this.partBody = partBody == null ? new JSONObject() : partBody;
        this.queryBody = queryBody == null ? new Body() : new Body(queryBody);
        this.type = type;
        this.multipartFile = multipartFile;
    }
}
