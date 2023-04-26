package com.black.core.api.tacitly;

import com.black.core.api.handler.ExampleStreamAdapter;
import com.black.core.api.pojo.ApiParameterDetails;
import com.black.utils.NameUtil;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Log4j2
public class TacitlyExampleStreamAdapter implements ExampleStreamAdapter {

    private List<ApiParameterDetails> apiParameterDetails;

    private final ApiAliasManger aliasManger;

    private final ApiDependencyManger dependencyManger;

    private final Map<String, String> exampleMap = new HashMap<>();

    private AbstractRequestBufferExampleReader requestBufferExampleReader;
    public TacitlyExampleStreamAdapter(ApiAliasManger aliasManger, ApiDependencyManger dependencyManger) {
        this.aliasManger = aliasManger;
        this.dependencyManger = dependencyManger;
    }

    public void setApiParameterDetails(List<ApiParameterDetails> apiParameterDetails) {
        this.apiParameterDetails = apiParameterDetails;
        for (ApiParameterDetails details : apiParameterDetails) {
            exampleMap.put(details.getName(), details.getType());
        }
    }

    public void setRequestBufferExampleReader(AbstractRequestBufferExampleReader requestBufferExampleReader) {
        this.requestBufferExampleReader = requestBufferExampleReader;
    }

    @Override
    public Collection<String> getExampleMap() {
        return exampleMap.keySet();
    }

    @Override
    public ExampleStreamAdapter addParam(String param) {
        return addParam(param, "String");
    }

    @Override
    public ExampleStreamAdapter addParam(String param, String type) {
        exampleMap.put(param, type);
        return this;
    }

    @Override
    public ExampleStreamAdapter addListParam(String param) {
        exampleMap.put(param, "List");
        return this;
    }

    @Override
    public ExampleStreamAdapter addAttributeParam(String param, Class<?>... pojoClass) {
        StringBuilder builder = new StringBuilder();
        for (Class<?> clazz : pojoClass) {
            String alias = NameUtil.getName(clazz);
            Class<?> pojo = aliasManger.queryPojo(alias);
            if (pojo == null){
                if (log.isErrorEnabled()) {
                    log.error("添加的属性, 应该提前被注册进去, pojoClass:{}, ", clazz);
                }
                throw new RuntimeException("需要提前注册:" + clazz);
            }
            builder.append(alias).append(",");
        }
        String entry = builder.toString();
        if (entry.endsWith(",")){
            entry = entry.substring(0, entry.length() - 1);
        }
        exampleMap.put(param, entry);
        return this;
    }

    @Override
    public ExampleStreamAdapter addAttributeListParam(String param, Class<?>... pojoClass) {
        StringBuilder builder = new StringBuilder();
        for (Class<?> clazz : pojoClass) {
            String alias = NameUtil.getName(clazz);
            Class<?> pojo = aliasManger.queryPojo(alias);
            if (pojo == null){
                if (log.isErrorEnabled()) {
                    log.error("添加的属性, 应该提前被注册进去, pojoClass:{}, ", clazz);
                }
                throw new RuntimeException("需要提前注册:" + clazz);
            }
            builder.append(alias).append(",");
        }
        String entry = builder.toString();
        if (entry.endsWith(",")){
            entry = entry.substring(0, entry.length() - 1);
        }
        exampleMap.put(param, "List-" + entry);
        return this;
    }

    @Override
    public ExampleStreamAdapter removeParam(String param) {
        exampleMap.remove(param);
        return this;
    }

    @Override
    public ExampleStreamAdapter removeIf(Predicate<? super String> filter) {
        for (String s : getExampleMap()) {
            if (filter.test(s)) {
                exampleMap.remove(s);
            }
        }
        return this;
    }

    @Override
    public ExampleStreamAdapter rename(String oldParam, String newParam) {
        if (exampleMap.containsKey(oldParam)){
            exampleMap.put(newParam, exampleMap.get(oldParam));
            exampleMap.remove(oldParam);
        }
        return this;
    }

    @Override
    public String rewrite() {
        return requestBufferExampleReader.writeStream(exampleMap);
    }

    public void clear(){
        if (exampleMap != null) {
            exampleMap.clear();
        }
    }
}
