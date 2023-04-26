package com.black.file;

import com.black.core.tools.BaseBean;
import lombok.Data;

@Data
public class FileEntity extends BaseBean<FileEntity> {

    private String fileName;

    private String id;

    private long size;

    private String uploadTime;

    private String type;

}
