package com.black.file;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class UploadBatchArrayArgWrapper extends AbstractArgWrapper<List<Map<String, Object>>>{


    private Map<String, Object> queryParams;

    private List<MultipartFile> files;

}
