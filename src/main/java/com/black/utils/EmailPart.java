package com.black.utils;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Getter
public class EmailPart {


    private final DataSource dataSource;

    //附件名称
    private String annexName;

    private String fileName;

    public static EmailPart input(InputStream in, String fileName) throws IOException {
        return input(in, fileName, null);
    }

    public static EmailPart input(InputStream in, String fileName, String annexName) throws IOException {
        EmailPart part = new EmailPart(in, fileName);
        if (annexName != null) part.setAnnexName(annexName);
        return part;
    }

    public static EmailPart multipart(MultipartFile multipartFile) throws IOException{
        return multipart(multipartFile, null);
    }

    public static EmailPart multipart(MultipartFile multipartFile, String annexName) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        EmailPart emailPart = new EmailPart(inputStream, multipartFile.getOriginalFilename());
        if (annexName != null) emailPart.setAnnexName(annexName);
        return emailPart;
    }

    public static EmailPart file(File file){
        return file(file, null);
    }

    public static EmailPart file(File file, String annexName){
        EmailPart part = new EmailPart(file);
        if (annexName != null) part.setAnnexName(annexName);
        return part;
    }

    public EmailPart(@NonNull File file) {
        dataSource = new FileDataSource(file);
        fileName = file.getName();
        annexName = fileName;
    }

    public EmailPart(InputStream in, String fileName) throws IOException {
        dataSource = new ByteArrayDataSource(in, FileTypeMap.getDefaultFileTypeMap().getContentType(fileName));
        this.fileName = fileName;
        annexName = fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setAnnexName(String annexName) {
        this.annexName = annexName;
    }
}
