package com.black.core.api.handler;

import com.black.core.api.pojo.ApiParameterDetails;

import java.util.List;

public interface RequestExampleReader {

    String handlerRequestParamExample(List<ApiParameterDetails> requestListDetails, String example, ExampleStreamAdapter adapter);
}
