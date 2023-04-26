package com.black.file;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @NoArgsConstructor @Getter
public class RequestPathFileEntity extends FileEntity implements FileEntityResultSetter{

    private String path;

    public RequestPathFileEntity(String path){
        this.path = path;
    }

    @Override
    public void setFileEntity(FileEntity entity) {
        wired(entity);
    }
}
