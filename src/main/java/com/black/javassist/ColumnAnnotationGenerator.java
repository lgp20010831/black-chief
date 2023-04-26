package com.black.javassist;

import com.black.template.jdbc.JavaColumnMetadata;

public interface ColumnAnnotationGenerator {


    CtAnnotations createAnnotations(JavaColumnMetadata javaColumnMetadata);

}
