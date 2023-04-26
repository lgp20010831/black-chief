package com.black.core.api.tacitly;

import com.black.core.api.handler.ExampleStreamAdapter;
import com.black.core.api.handler.RequestExampleReader;
import com.black.core.api.pojo.ApiParameterDetails;

import java.util.List;

public class TacitlyRequestExampleReader implements RequestExampleReader {

    @Override
    public String handlerRequestParamExample(List<ApiParameterDetails> requestListDetails, String example, ExampleStreamAdapter adapter) {
        return example;
    }
}
