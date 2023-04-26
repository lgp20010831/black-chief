package com.black.file;

import com.black.io.minio.MinioBody;
import com.black.core.minio.Minios;

import java.io.InputStream;

public class DefaultMinioFileSaver implements FileSaver<Object> {

    @Override
    public RequestPathFileEntity saveFile(InputStream in, FileEntity entity) {
        MinioBody minioBody = Minios.def().upload(in, entity.getFileName(), null);
        return new RequestPathFileEntity(minioBody.getUrl());
    }

    @Override
    public InputStream download(FileEntity entity) {
        return Minios.def().getFile(entity.getFileName());
    }

    @Override
    public boolean deleteFile(FileEntity entity) {
        Minios.def().removeFile(entity.getFileName());
        return true;
    }
}
