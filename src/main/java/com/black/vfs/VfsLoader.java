package com.black.vfs;

import java.io.IOException;
import java.util.List;


public class VfsLoader extends AbstractVfsLoader {

    private final VFS vfs;

    public VfsLoader(){
        this(null);
    }

    public VfsLoader(ClassLoader loader) {
        setClassLoader(loader);
        vfs = VFS.getInstance();
    }


    @Override
    protected List<String> listFile(String packagePath) throws IOException {
        return vfs.list(packagePath);
    }

}
