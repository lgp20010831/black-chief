package com.black.vfs;

import com.black.core.util.StreamUtils;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface VfsScanner {


    Set<Class<?>> load(String classPath);

    void setClassLoader(ClassLoader classLoader);

    List<String> fileNameList(String scanPath);

    default List<File> fileList(String scanPath){
        return StreamUtils.mapList(fileNameList(scanPath), File::new);
    }
}
