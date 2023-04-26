package com.black.project;

import com.black.function.FileFilter;
import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;
import lombok.NonNull;

import java.io.File;
import java.util.List;

public abstract class AbstractFileFilterExecutive {


    protected final FileFilter fileFilter;

    public AbstractFileFilterExecutive(@NonNull FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    protected List<File> getResource(String classPath){
        VfsScanner vfsScanner = VFS.findVfsScanner();
        return vfsScanner.fileList(classPath);
    }

}
