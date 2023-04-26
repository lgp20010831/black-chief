package com.black.vfs;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringBootVfsLoader extends AbstractVfsLoader{

    private final ResourcePatternResolver resourceResolver;

    public SpringBootVfsLoader() {
        this.resourceResolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
    }

    public ResourcePatternResolver getResourceResolver() {
        return resourceResolver;
    }

    public boolean isValid() {
        return true;
    }

    private static String preserveSubpackageName(final String baseUrlString, final Resource resource,
                                                 final String rootPath) {
        try {
            return rootPath + (rootPath.endsWith(StringPool.SLASH) ? StringPool.EMPTY : StringPool.SLASH)
                    + resource.getURL().toString().substring(baseUrlString.length());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected static List<URL> getResources(String path) throws IOException {
        return Collections.list(Thread.currentThread().getContextClassLoader().getResources(path));
    }

    public List<String> list(String path) throws IOException {
        List<String> names = new ArrayList<>();
        for (URL url : getResources(path)) {
            names.addAll(list(url, path));
        }
        return names;
    }

    protected List<String> list(URL url, String path) throws IOException {
        String urlString = url.toString();
        String baseUrlString = urlString.endsWith(StringPool.SLASH) ? urlString : urlString.concat(StringPool.SLASH);
        Resource[] resources = resourceResolver.getResources(baseUrlString + "**/*.class");
        return Stream.of(resources).map(resource -> preserveSubpackageName(baseUrlString, resource, path))
                .collect(Collectors.toList());
    }

    @Override
    protected List<String> listFile(String packagePath) throws IOException {
        return list(packagePath);
    }
}
