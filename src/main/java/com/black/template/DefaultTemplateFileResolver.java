package com.black.template;

import com.black.core.mvc.FileUtil;

import java.io.File;


public class DefaultTemplateFileResolver implements TemplateFileResolver{


    @Override
    public void resolver(String fileName, String generatePath, boolean isResource, String buffer) {
         if (isResource){
             processorResourceFile(fileName, generatePath, buffer);
         }else {
             processorClassFile(fileName, generatePath, buffer);
         }
    }


    public void processorClassFile(String fileName, String generatePath, String buffer){
        String path = FileUtil.getFilePath(generatePath);
        FileUtil.createDir(path);
        String filePath = path + "\\" + fileName;
        File file = FileUtil.dropAndcreateFile(filePath);
        FileUtil.writerFile(file, buffer);
    }

    public void processorResourceFile(String fileName, String generatePath, String buffer){
        String mapperFilePath = FileUtil.getResourceFilePath(generatePath);
        FileUtil.createDir(mapperFilePath);
        mapperFilePath = mapperFilePath + "\\" + fileName;
        File resourceFile = FileUtil.dropAndcreateFile(mapperFilePath);
        FileUtil.writerFile(resourceFile, buffer);
    }
}
