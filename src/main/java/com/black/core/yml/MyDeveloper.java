package com.black.core.yml;

import com.black.callback.develop.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 李桂鹏
 * @create 2023-05-17 15:49
 */
@EnabledByAnnotation(SpringBootApplication.class)
@SuppressWarnings("all")
public class MyDeveloper implements Developer, EnabledAnnotationAware<SpringBootApplication> {


    @Override
    public void pullAnnotation(SpringBootApplication annotation) {
        System.out.println(annotation);
    }
}
