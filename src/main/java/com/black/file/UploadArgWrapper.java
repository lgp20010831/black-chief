package com.black.file;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Getter @Setter
public class UploadArgWrapper extends AbstractArgWrapper<JSONObject> {

    private Map<String, Object> queryParams;

    private MultipartFile file;

}
