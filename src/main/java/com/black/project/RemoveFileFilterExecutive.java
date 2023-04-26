package com.black.project;

import com.black.function.FileFilter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.List;

@Log4j2
public class RemoveFileFilterExecutive extends AbstractFileFilterExecutive{

    public RemoveFileFilterExecutive(@NonNull FileFilter fileFilter) {
        super(fileFilter);
    }

    public void removeFiles(String classPath){
        removeFiles(getResource(classPath));
    }

    public void removeFiles(List<File> files){
        for (File file : files) {
            if (file == null || !file.exists()){
                continue;
            }
            if (!fileFilter.containFile(file)) {
               continue;
            }
            File target = fileFilter.replaceFile(file);
            if (target == null){
                continue;
            }
            if (!target.exists()){
                log.info("[file filter] replace file:{} is not exists", target.getPath());
                continue;
            }
            if (fileFilter.judge(target)) {
                log.info("[file filter] delete file: {}", target.getPath());
                if (!target.delete()) {
                    log.info("[file filter] delete file: {} fail", target.getPath());
                }
            }
        }
    }
}
