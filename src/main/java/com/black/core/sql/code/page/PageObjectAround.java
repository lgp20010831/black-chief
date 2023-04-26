package com.black.core.sql.code.page;

import com.black.page.PageCache;
import com.black.core.aop.servlet.*;
import com.black.core.sql.annotation.OpenSqlPage;
import com.black.core.sql.annotation.WriedSQLPageObject;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

import static com.black.core.response.Code.SUCCESS;
import static com.black.core.response.VariablePool.WORK_SUCCESSFUL;

@GlobalAround
public class PageObjectAround implements GlobalAroundResolver {

    @Override
    public Object[] beforeInvoke(Object[] args, HttpMethodWrapper httpMethodWrapper) {
        Method method = httpMethodWrapper.getHttpMethod();
        if (httpMethodWrapper.parameterHasAnnotation(WriedSQLPageObject.class)) {
            ParameterWrapper parameter = httpMethodWrapper.getSinglonParameterByAnnotation(WriedSQLPageObject.class);
            if (SQLPageObject.class.isAssignableFrom(parameter.getType())){
                SQLPageObject<?> pageObject;
                OpenSqlPage sqlPage = AnnotationUtils.getAnnotation(method, OpenSqlPage.class);
                if (sqlPage != null){
                    Page<?> page = PageCache.getPage();
                    pageObject = new SQLPageObject<>(page, httpMethodWrapper);
                }else {
                    pageObject = new SQLPageObject<>(httpMethodWrapper);
                }
                args[parameter.getIndex()] = pageObject;
            }
        }
        return args;
    }

    @Override
    public Object handlerAfterInvoker(Object result, HttpMethodWrapper httpMethodWrapper, Class<? extends RestResponse> responseClass) {
        if (result instanceof SQLPageObject){
            SQLPageObject<?> pageObject = (SQLPageObject<?>) result;
            RestResponse response = AopControllerIntercept.createRestResponse(responseClass, SUCCESS.value(), true, WORK_SUCCESSFUL, httpMethodWrapper);
            response.setResult(pageObject.getSource());
            response.setTotal(pageObject.getTotal());
            return response;
        }
        return result;
    }
}
