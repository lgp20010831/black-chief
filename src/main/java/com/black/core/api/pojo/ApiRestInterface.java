package com.black.core.api.pojo;

import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString
public class ApiRestInterface {

    private String remark;

    private List<String> urls;

    private List<String> httpMethods;

    private Map<String, String> requestHeaders;

    private List<ApiParameterDetails> requestListDetails;

    private String requestExample;

    private String responseExample;
}
