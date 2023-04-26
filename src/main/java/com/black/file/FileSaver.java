package com.black.file;

import java.io.IOException;
import java.io.InputStream;

public interface FileSaver<R> {


    R saveFile(InputStream in, FileEntity entity) throws IOException;

    InputStream download(FileEntity entity);

    default boolean deleteFile(FileEntity entity){
        throw new UnsupportedOperationException("un support delete file");
    }
}
