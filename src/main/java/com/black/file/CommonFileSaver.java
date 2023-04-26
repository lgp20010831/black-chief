package com.black.file;

import com.black.core.mvc.FileUtil;
import com.black.core.util.Assert;
import com.black.utils.IoUtils;

import java.io.*;

public class CommonFileSaver implements FileSaver<Object>{

    public static String BASE_PATH;

    @Override
    public RequestPathFileEntity saveFile(InputStream in, FileEntity entity) throws IOException {
        Assert.notNull(BASE_PATH, "SET BASE PATH");
        String filePath = BASE_PATH + "\\" + entity.getFileName();
        File file = FileUtil.dropAndcreateFile(filePath);
        FileUtil.writerFile(file, IoUtils.read(in));
        return new RequestPathFileEntity(filePath);
    }

    @Override
    public InputStream download(FileEntity entity) {
        Assert.notNull(BASE_PATH, "SET BASE PATH");
        String filePath = BASE_PATH + "\\" + entity.getFileName();
        try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public boolean deleteFile(FileEntity entity) {
        Assert.notNull(BASE_PATH, "SET BASE PATH");
        String filePath = BASE_PATH + "\\" + entity.getFileName();
        File file = new File(filePath);
        return file.delete();
    }
}
