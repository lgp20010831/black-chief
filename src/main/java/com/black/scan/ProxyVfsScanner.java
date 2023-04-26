package com.black.scan;

import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;

import java.util.List;
import java.util.Set;

public class ProxyVfsScanner extends AbstractChiefScanner{


    private final VfsScanner vfsScanner;

    public ProxyVfsScanner() {
        vfsScanner = VFS.findVfsScanner();
    }

    @Override
    public Set<Class<?>> load(String classpath) {
        return vfsScanner.load(classpath);
    }

    @Override
    public List<String> fileNameList(String scanPath) {
        return vfsScanner.fileNameList(scanPath);
    }
}
