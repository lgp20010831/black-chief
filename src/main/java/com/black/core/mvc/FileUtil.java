package com.black.core.mvc;

import com.black.core.util.StringUtils;
import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;
import lombok.extern.log4j.Log4j2;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Log4j2 @SuppressWarnings("all")
public class FileUtil {

    public static final String DevelopmentPath = "\\src\\main\\java";

    public static final String DevelopmentResourcePath = "\\src\\main\\resources";

    public static String getExpand(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1, filename.length());
    }

    public interface FileRemoveFilter{

        default File replaceFile(File file){
            return file;
        }

        boolean isRemove(File file);

    }

    public static File castClassFileToIdeaPathJavaFile(File classFile){
        if (classFile == null){
            return null;
        }
        String path = classFile.getPath();
        String javaPath = castClassPathToIdeaPathJavaFile(path);
        return new File(javaPath);
    }

    public static String castClassPathToIdeaPathJavaFile(String classFilePath){
        if (!classFilePath.endsWith(".class")){
            return classFilePath;
        }
        String basicPath = getBasicPath();
        if (!basicPath.endsWith("\\")){
            basicPath += "\\";
        }
        String partiallyProductsJavaPath = StringUtils.removeIfEndWith(classFilePath, ".class");
        partiallyProductsJavaPath = StringUtils.removeIfStartWith(partiallyProductsJavaPath, "\\");
        return StringUtils.linkStr(basicPath, partiallyProductsJavaPath, ".java");
    }

    public static void removeFile(String scanPath, FileRemoveFilter fileRemoveFilter){
        VfsScanner vfsScanner = VFS.findVfsScanner();
        List<File> files = vfsScanner.fileList(scanPath);
        for (File file : files) {
            if (fileRemoveFilter != null){
                File target = fileRemoveFilter.replaceFile(file);
                if (target == null) continue;
                if (!target.exists()){
                    log.info("[file filter] replace file:{} is not exists", target.getPath());
                    continue;
                }
                if (fileRemoveFilter.isRemove(target)) {
                    log.info("[file filter] delete file: {}", target.getPath());
                    if (!target.delete()) {
                        log.info("[file filter] delete file: {} fail", target.getPath());
                    }
                }
            }
        }
    }

    public static void clearFiles(String path){
        String filePath = getFilePath(path);
        File file = new File(filePath);
        if (file.exists()){
            File[] listFiles = file.listFiles();
            for (File listFile : listFiles) {
                if (!listFile.isDirectory()) {
                    if (listFile.getName().endsWith(".java")) {
                        listFile.delete();
                    }
                }
            }
        }
    }


    public static String getFilePath(String relativePath){
        if (relativePath == null){
            return null;
        }
        String systemPath = System.getProperty("user.dir");
        if (systemPath == null){
            return null;
        }
        return StringUtils.linkStr(systemPath, DevelopmentPath, "\\", replacePlaceholder(relativePath));
    }

    public static void createClassCatalogue(String classPath){
        String placeholder = replacePlaceholder(classPath);
        String filePath = getBasicPath() + "\\" + placeholder;
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static String getBasicPath(){
        String systemPath = System.getProperty("user.dir");
        return StringUtils.linkStr(systemPath, DevelopmentPath);
    }

    public static String getResourceFilePath(String relativePath){
        if (relativePath == null){
            return null;
        }
        String systemPath = System.getProperty("user.dir");
        if (systemPath == null){
            return null;
        }
        return StringUtils.linkStr(systemPath, DevelopmentResourcePath, "\\", replacePlaceholder(relativePath));
    }

    public static void createDir(String path){
        File file = new File(path);
        file.mkdirs();
    }

    public static File dropAndcreateFile(String filePath){
        File file = new File(filePath);
        if (file.exists()){
            if (!file.delete()) {
                throw new IllegalStateException("无法删除原有文件: " + filePath);
            }
        }
        try {
            if (!file.createNewFile()) {
                throw new IllegalStateException("create file fail: " + filePath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("create file fail: " + filePath, e);
        }
        return file;
    }

    public static File createFile(String filePath){
        File file = new File(filePath);
        if (!file.exists()){
            file.mkdirs();
            try {
                if (!file.createNewFile()) {
                    return null;
                }
            } catch (IOException e) {
                throw new RuntimeException("create file fail", e);
            }
        }
        return file;
    }

    public static void writerFile(File file, String data){
        try (FileWriter writer = new FileWriter(file)){
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("write file fail", e);
        }
    }


    public static String replacePlaceholder(String filePath){
        String replacePath = filePath.replace(".", "\\");
        if (replacePath.startsWith("\\")){
            replacePath = replacePath.substring(1);
        }
        return replacePath;
    }

    public static String determineRootDir(String location) {
        int prefixEnd = location.indexOf(':') + 1;
        int rootDirEnd = location.length();
        while (rootDirEnd > prefixEnd && isPattern(location.substring(prefixEnd, rootDirEnd))) {
            rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
        }
        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }
        return location.substring(0, rootDirEnd);
    }

    public static boolean isPattern(@Nullable String path) {
        if (path == null) {
            return false;
        }
        boolean uriVar = false;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '*' || c == '?') {
                return true;
            }
            if (c == '{') {
                uriVar = true;
                continue;
            }
            if (c == '}' && uriVar) {
                return true;
            }
        }
        return false;
    }
}
