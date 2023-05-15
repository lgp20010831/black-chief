package com.black.ftl;

import com.black.core.factory.beans.xml.XmlMessage;
import com.black.core.factory.beans.xml.XmlWrapper;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.throwable.IOSException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-05-13 15:42
 */
@SuppressWarnings("all") @Log4j2
public class ResourceUtils {

    private static PathMatchingResourcePatternResolver resolver =
            new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());


    public static List<Resource> getResources(String location, String fileType){
        fileType = StringUtils.addIfNotStartWith(fileType, ".");
        location = checkLocation(location, fileType);
        log.info("[RESOURCE] resolve location: {}", location);
        try {
            Resource[] resources = resolver.getResources(location);
            return Arrays.asList(resources);
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }

    //xml-sql/
    private static String checkLocation(String location, String fileType){
        if (!location.endsWith(fileType)){
            if (location.endsWith("/")){
                location = location + "**/**" + fileType;
            }else {
                location = location + "/**/**" + fileType;
            }
        }

        if (!location.startsWith("classpath*:") && !location.startsWith("classpath:")){
            location = "classpath*:" + location;
        }
        return location;
    }
}
