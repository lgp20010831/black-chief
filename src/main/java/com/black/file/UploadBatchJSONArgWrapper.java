package com.black.file;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UploadBatchJSONArgWrapper extends AbstractArgWrapper<JSONObject>{

    private Map<String, Object> queryParams;

    private List<MultipartFile> files;

}
