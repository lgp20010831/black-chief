package com.black.callback.develop;

import java.lang.annotation.Annotation;

/**
 * @author 李桂鹏
 * @create 2023-05-17 16:11
 */
@SuppressWarnings("all")
public interface EnabledAnnotationAware<T extends Annotation> {

    void pullAnnotation(T annotation);

}
