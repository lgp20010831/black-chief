package com.black.xml;

import com.black.core.factory.beans.xml.XmlMessage;
import com.black.core.factory.beans.xml.XmlWrapper;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;
import com.black.throwable.IOSException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-05-08 11:43
 */
@SuppressWarnings("all")
public class XmlUtils {

    private static final IoLog log = LogFactory.getArrayLog();

    private static PathMatchingResourcePatternResolver resolver =
            new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());

    public static List<XmlWrapper> getXmlResources(String location){
        location = checkLocation(location);
        log.info("[XML] resolve location: {}", location);
        try {
            Resource[] resources = resolver.getResources(location);
            return StreamUtils.mapList(Arrays.asList(resources), resource -> {
                try {
                    log.info("[XML] read xml file: {}", resource.getFilename());
                    return new XmlWrapper(new XmlMessage(resource.getInputStream()));
                } catch (IOException e) {
                    throw new IOSException(e);
                }
            });
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }

    //xml-sql/
    private static String checkLocation(String location){
        if (!location.endsWith(".xml")){
            if (location.endsWith("/")){
                location = location + "**/**.xml";
            }else {
                location = location + "/**/**.xml";
            }
        }

        if (!location.startsWith("classpath*:") && !location.startsWith("classpath:")){
            location = "classpath*:" + location;
        }
        return location;
    }

    public static String compressSql(String sql){
        StringBuilder builder = new StringBuilder();
        boolean beforeBlank = false;
        boolean beforeWrap = false;
        for (char c : sql.toCharArray()) {
            if (c == '\n' || c == '\t'){
                if (!beforeWrap){
                    builder.append(" ");
                    beforeWrap = true;
                }
            }else if (c == ' '){
                if (!beforeBlank){
                    builder.append(c);
                    beforeBlank = true;
                }
            }else {
                builder.append(c);
                beforeWrap = false;
                beforeBlank = false;
            }
        }
        return builder.toString();
    }
}
