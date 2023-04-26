package com.black.core.factory.beans.xml;

import com.black.core.factory.beans.BeanFactorysException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ClassLoaderResourceReader implements ResourceReader{
    @Override
    public InputStream reader(String path) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        try {
            return resource.openStream();
        } catch (IOException e) {
            throw new BeanFactorysException(e);
        }
    }
}
