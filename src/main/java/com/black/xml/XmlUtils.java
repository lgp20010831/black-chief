package com.black.xml;

import com.black.core.factory.beans.xml.XmlMessage;
import com.black.core.factory.beans.xml.XmlWrapper;
import com.black.core.json.Trust;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.tools.BaseBean;
import com.black.core.util.StreamUtils;
import com.black.sql_v2.serialize.SerializeUtils;
import com.black.throwable.IOSException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-08 11:43
 */
@SuppressWarnings("all")
public class XmlUtils {

    private static final IoLog log = LogFactory.getArrayLog();

    private static PathMatchingResourcePatternResolver resolver =
            new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());


    public static List<XmlWrapper> getFtlResources(String location){
        location = checkFtlLocation(location);
        log.info("[XML] resolve ftl location: {}", location);
        try {
            Resource[] resources = resolver.getResources(location);
            return StreamUtils.mapList(Arrays.asList(resources), resource -> {
                try {
                    log.info("[XML] read ftl file: {}", resource.getFilename());
                    return new XmlWrapper(new XmlMessage(resource.getInputStream()));
                } catch (IOException e) {
                    throw new IOSException(e);
                }
            });
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }

    private static String checkFtlLocation(String location){
        if (!location.endsWith(".ftl")){
            if (location.endsWith("/")){
                location = location + "**/**.ftl";
            }else {
                location = location + "/**/**.ftl";
            }
        }

        if (!location.startsWith("classpath*:") && !location.startsWith("classpath:")){
            location = "classpath*:" + location;
        }
        return location;
    }

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

    public static Map<Object, Object> castIndexMap(Object... params){
        Map<Object, Object> map = new LinkedHashMap<>();
        int i = 1;
        for (Object param : params) {
            map.put(i++, param);
        }
        return map;
    }

    public static Map<String, Object> makeEnv(Object... params){
        Map<String, Object> env = new LinkedHashMap<>();
        for (Object param : params) {
            if (param == null){
                continue;
            }
            Class<?> paramClass = param.getClass();
            if (param instanceof Map){
                env.putAll((Map<? extends String, ?>) param);
            }else if (param instanceof BaseBean || paramClass.isAnnotationPresent(Trust.class)){
                env.putAll(SerializeUtils.serialize(param));
            }
        }
        env.putAll(XmlSql.castParams(params));
        return env;
    }
}
