package com.black.ftl;

import com.alibaba.fastjson.JSONObject;
import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.factory.beans.xml.XmlMessage;
import com.black.core.factory.beans.xml.XmlWrapper;
import com.black.core.json.JsonUtil;
import com.black.core.json.JsonUtils;
import com.black.core.util.Assert;
import com.black.syntax.SyntaxResolverManager;
import com.black.throwable.IOSException;
import com.black.utils.ServiceUtils;
import com.black.xml.engine.XmlResolveEngine;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 等于一个 ftl 文件
 * @author 李桂鹏
 * @create 2023-05-13 15:40
 */
@SuppressWarnings("all")
public class FtlNameSpace implements NameSpace{

    private String resourceName;

    private final XmlWrapper wrapper;

    private final FtlResolver resolver;

    private final Map<String, ElementWrapper> models = new LinkedHashMap<>();

    public FtlNameSpace(Resource resource, FtlResolver resolver){
        resourceName = resource.getFilename();
        this.resolver = resolver;
        try {
            InputStream inputStream = resource.getInputStream();
            wrapper = new XmlWrapper(new XmlMessage(inputStream));
            resolveWrapper();
        } catch (IOException e) {
            throw new IOSException(e);
        }
    }

    private void resolveWrapper(){
        ElementWrapper rootElement = wrapper.getRootElement();
        List<ElementWrapper> wrappers = rootElement.getsByName("model");
        for (ElementWrapper ew : wrappers) {
            String id = ew.getAttrVal("id");
            Assert.notNull(id, "model is must is not null");
            models.put(id, ew);
        }
    }

    private String invoke(ElementWrapper model, Object env){
        XmlResolveEngine engine = resolver.getXmlResolveEngine();
        JSONObject json = JsonUtils.letJson(env);
        String text = engine.resolve(model, json);
        text = ServiceUtils.parseTxt(text, "${", "}", item -> {
            Object val = SyntaxResolverManager.resolverItem(item, json, null);
            if (val == null){
                return resolver.getNullDefaultValue();
            }else {
                return val.toString();
            }
        });
        return text;
    }

    @Override
    public String resolve(Object data) {
        return resolveParts(data);
    }

    @Override
    public String resolveParts(StringJoiner joiner, Object data) {
        Map<String, ElementWrapper> temp = new LinkedHashMap<>(models);
        for (ElementWrapper ew : temp.values()) {
            String text = invoke(ew, data);
            joiner.add(text);
        }
        return joiner.toString();
    }

    @Override
    public String resolvePart(String id, Object data) {
        ElementWrapper model = models.get(id);
        return invoke(model, data);
    }
}
