package com.black.graphql.core.request;


import com.black.graphql.core.request.param.RequestParameter;
import com.black.graphql.core.request.result.ResultAttributtes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@SuppressWarnings("all")
public abstract class GraphqlRequest {
    protected String requestName;
    protected RequestParameter requestParameter;
    protected List<ResultAttributtes> resultAttributes = new ArrayList();

    protected GraphqlRequest(String requestName) {
        this.requestName = requestName;
    }

    public RequestParameter addParameter(String key, Object val) {
        return this.getRequestParameter().addParameter(key, val);
    }

    public RequestParameter getRequestParameter() {
        if (this.requestParameter == null) {
            this.requestParameter = RequestParameter.build();
        }

        return this.requestParameter;
    }

    public GraphqlRequest addResultAttributes(String... resultAttr) {
        if (resultAttr != null && resultAttr.length > 0) {
            String[] var2 = resultAttr;
            int var3 = resultAttr.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String str = var2[var4];
                ResultAttributtes ra = new ResultAttributtes(str);
                this.resultAttributes.add(ra);
            }
        }

        return this;
    }

    public GraphqlRequest addResultAttributes(ResultAttributtes... resultAttr) {
        if (resultAttr != null && resultAttr.length > 0) {
            ResultAttributtes[] var2 = resultAttr;
            int var3 = resultAttr.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                ResultAttributtes ra = var2[var4];
                this.resultAttributes.add(ra);
            }
        }

        return this;
    }

    public String getRequestName() {
        return this.requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public String toString() {
        StringBuffer requestBuffer = new StringBuffer(this.requestName);
        String paramStr = this.getRequestParameter().toString();
        StringBuffer resultAttrBuffer = new StringBuffer("");
        boolean first = true;

        ResultAttributtes ra;
        for(Iterator var5 = this.resultAttributes.iterator(); var5.hasNext(); resultAttrBuffer.append(ra.toString())) {
            ra = (ResultAttributtes)var5.next();
            if (first) {
                first = false;
            } else {
                resultAttrBuffer.append(" ");
            }
        }

        String resultAttrStr = resultAttrBuffer.toString();
        requestBuffer.append(paramStr);
        requestBuffer.append("{");
        requestBuffer.append(resultAttrStr);
        requestBuffer.append("}");
        return requestBuffer.toString();
    }
}
