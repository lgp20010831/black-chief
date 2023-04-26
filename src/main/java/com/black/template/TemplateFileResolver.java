package com.black.template;

import java.io.IOException;

public interface TemplateFileResolver {

    void resolver(String fileName, String generatePath, boolean isResource, String buffer) throws IOException;

}
