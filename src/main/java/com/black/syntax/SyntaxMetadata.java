package com.black.syntax;

import com.black.core.query.FieldWrapper;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SyntaxMetadata {

    String path;

    String name;

    boolean field;

    FieldWrapper fieldMetadata;

    Object parent;

    Object environment;

    Object value;

}
