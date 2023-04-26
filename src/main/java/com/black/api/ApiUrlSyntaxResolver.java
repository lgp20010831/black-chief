package com.black.api;

import com.black.core.util.StreamUtils;
import com.black.core.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;


@SuppressWarnings("all")
public class ApiUrlSyntaxResolver extends AbstractApiRequestSyntaxResolver {

    public static boolean parseUrl = false;

    public ApiUrlSyntaxResolver(){
        super("url:");
    }

    @Override
    public void resolveHttpMethod(String expression, HttpMethod httpMethod, Map<String, Object> source) {
        List<String> requestUrl = httpMethod.getRequestUrl();
        String p = StringUtils.removeIfStartWith(expression, "?");
        String[] split = p.split("&");
        String urlValue = expression;
        if (parseUrl){
            StringJoiner joiner = new StringJoiner("&", "?", "");
            for (String s : split) {
                String[] strings = StringUtils.split(s, "=", 2, "error url style:" + expression);
                String string = strings[1];
                String value = null;
                try {
                    Integer.parseInt(string);
                    value = "0";
                }catch (Throwable e){}
                if (value == null){
                    try {
                        Boolean.parseBoolean(string);
                        value = "false";
                    }catch (Throwable e){}
                }
                if (value == null){
                    value = ApiV2Utils.wriedString(value);
                }
                joiner.add(strings[0] + "=" + value);
            }
            urlValue = joiner.toString();
        }
        String finalUrlValue = urlValue;
        httpMethod.setRequestUrl(StreamUtils.mapList(requestUrl, url -> url + finalUrlValue));
        httpMethod.setRequestDome("");
    }
}
