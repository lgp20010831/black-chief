package com.black.scan;

import com.black.core.util.StreamUtils;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface ChiefScanner {

    /** scan and load class in classpath */
    Set<Class<?>> load(String classpath);

    void setClassLoader(ClassLoader classLoader);

    default List<String> fileNameList(String scanPath){
        throw new IllegalStateException("unsupport scan file name list");
    }

    default List<File> fileList(String scanPath){
        return StreamUtils.mapList(fileNameList(scanPath), File::new);
    }
}
