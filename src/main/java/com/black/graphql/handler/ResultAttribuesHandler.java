package com.black.graphql.handler;

import com.black.graphql.GraphqlHandler;
import com.black.graphql.GraphqlObject;
import com.black.graphql.Utils;
import com.black.graphql.annotation.OnlyAttribues;
import com.black.graphql.annotation.ResultAttributes;
import com.black.core.query.MethodWrapper;
import org.mountcloud.graphql.request.result.ResultAttributtes;

public class ResultAttribuesHandler implements GraphqlHandler {

    @Override
    public boolean supportPrepare(MethodWrapper mw) {
        return mw.hasAnnotation(ResultAttributes.class) ||
                mw.getDeclaringClassWrapper().hasAnnotation(ResultAttributes.class);
    }

    @Override
    public void doPrepare(MethodWrapper mw, GraphqlObject object) {
        ResultAttributes mwAnnotation = mw.getAnnotation(ResultAttributes.class);
        if (mwAnnotation != null){
            ResultAttributtes[] attributtes = Utils.parseResultAttributes(mwAnnotation.value());
            if (attributtes != null){
                object.getRequest().addResultAttributes(attributtes);
            }
        }

        if (!mw.hasAnnotation(OnlyAttribues.class)){
            ResultAttributes annotation = mw.getDeclaringClassWrapper().getAnnotation(ResultAttributes.class);
            if (annotation != null){
                ResultAttributtes[] attributtes = Utils.parseResultAttributes(annotation.value());
                if (attributtes != null){
                    object.getRequest().addResultAttributes(attributtes);
                }
            }
        }
    }
}
