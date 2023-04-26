package com.black.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.aop.servlet.RestResponse;
import com.black.core.json.UCJsonParser;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;

public class DefaultApiParser extends AbstractApiParser implements ConfigurationAware{

    private FormatParser formatParser;

    private Configuration configuration;

    public DefaultApiParser() {
    }

    @Override
    public boolean support(MethodWrapper mw) {
        return mw.hasAnnotation(ApiMethod.class);
    }

    @Override
    public void doResolve(MethodWrapper mw, HttpMethod method, Configuration configuration, Class<?> type) {
        Assert.notNull(formatParser, "formatParser is null");
        ApiMethod apiMethod = mw.getAnnotation(ApiMethod.class);
        ClassWrapper<?> cw = ClassWrapper.get(type);

        String requestFormat = apiMethod.requestFormat();
        processorRequest(method, requestFormat);

        String responseFormat = apiMethod.responseFormat();
        processorResponse(method, responseFormat, type);

        String[] headers = apiMethod.headers();
        handleHeaders(cw, method, headers, configuration, mw);

        setRemarkOfMethod(mw, method, apiMethod.remark());
    }


    protected void processorRequest(HttpMethod method, String requestFormat){
        if (StringUtils.hasText(requestFormat)){
            String demo = formatParser.parse(requestFormat);
            method.setRequestDome(demo);
            method.setRequestInvokeDome(demo);
            method.setRequestJSON(formatParser.parseJSON(requestFormat));
        }
    }


    protected boolean isArray(String format){
        return format.startsWith("[");
    }

    protected void processorResponse(HttpMethod method, String responseFormat, Class<?> type){
        ClassWrapper<? extends RestResponse> responseClass = configuration.getResponseClass(type);
        //先构造出响应类的 json
        JSONObject responseJson = new JSONObject();
        formatParser.handler0(responseJson, responseClass);
        UCJsonParser parser = formatParser.getParser();
        if (StringUtils.hasText(responseFormat)){


            //在判断http方法上的响应格式
            //最终要存入响应类 json 里
            if (isArray(responseFormat)){
                JSONArray array = parser.parseArray(responseFormat);
                responseJson.put("result", formatParser.handlerDependencyArray(array));
            }else {
                JSONObject json = parser.parseJson(responseFormat);
                responseJson.put("result", formatParser.handlerDependencyJson(json));
            }
        }
        method.setResponseDome(JSONTool.formatJson(responseJson.toString()));
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        formatParser = new FormatParser(configuration);
    }
}
