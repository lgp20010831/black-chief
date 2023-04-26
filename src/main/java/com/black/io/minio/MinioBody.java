package com.black.io.minio;

import com.black.JsonBean;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MinioBody extends JsonBean {

    private String url;

    private String objectName;
}
