package com.black.core.http.code;

import com.alibaba.fastjson.JSONObject;
import com.black.core.http.annotation.*;

import java.util.Map;

@OpenHttp("#{http_prefix}")
public interface HttpMapper {

    @PostUrl("#{post_url}")
    String doPost(@JsonBody JSONObject body, @UrlMap Map<String, Object> urlParam, @HttpHeaders Map<String, String> headers);

    @GetUrl("#{get_url}")
    String doGet(@UrlMap Map<String, Object> urlParams, @HttpHeaders Map<String, String> headers);

    @PartUrl("#{part_url}")
    String doPart(@UrlMap Map<String, Object> urlParams, @HttpPart JSONObject json,
                  @HttpHeaders Map<String, String> headers, @HttpPart Object... files);
}
