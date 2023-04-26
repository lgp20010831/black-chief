package com.black.core.aop.servlet;

import com.black.core.cache.AopControllerStaticCache;
import com.black.core.query.MethodWrapper;
import com.github.pagehelper.Page;


import static com.black.core.aop.servlet.AopControllerIntercept.pageNumName;
import static com.black.core.aop.servlet.AopControllerIntercept.pageSizeName;
import static com.black.core.response.Code.SUCCESS;
import static com.black.core.response.VariablePool.WORK_SUCCESSFUL;

@GlobalAround
public class PageInjectionResolver implements GlobalAroundResolver {

    private AopControllerIntercept controllerIntercept;

    @Override
    public Object[] handlerArgs(Object[] args, HttpMethodWrapper httpMethodWrapper) {
        MethodWrapper wrapper = httpMethodWrapper.getMethodWrapper();
        OpenAutonomyPaging autonomyPaging = wrapper.getAnnotation(OpenAutonomyPaging.class);
        if (httpMethodWrapper.isPage() || autonomyPaging != null) {
            if(controllerIntercept == null){
                controllerIntercept = AopControllerStaticCache.getControllerIntercept();
            }

            if (controllerIntercept != null){
                Page<Object> objectPage = null;
                if (autonomyPaging == null){
                    ThreadLocal<Page<Object>> pageThreadLocal = controllerIntercept.getPageThreadLocal();
                    objectPage = pageThreadLocal.get();
                }else {
                    if (pageNumName == null || pageSizeName == null || !autonomyPaging.priority()) {
                        httpMethodWrapper.setPageNumArgName(pageNumName);
                        httpMethodWrapper.setPageSizeArgName(pageSizeName);
                    }else {
                        httpMethodWrapper.setPageNumArgName(autonomyPaging.pageNum());
                        httpMethodWrapper.setPageSizeArgName(autonomyPaging.pageSize());
                    }
                }

                ParameterWrapper parameterWrapper = wrapper.getSingleParameterByAnnotation(WriedPageObject.class);
                if (parameterWrapper != null && parameterWrapper.getType().equals(PageObject.class)){
                    PageObject<?> pageObject = new PageObject<>(objectPage, httpMethodWrapper);
                    args[parameterWrapper.getIndex()] = pageObject;
                }
            }
        }
        return args;
    }

    @Override
    public Object handlerAfterInvoker(Object result, HttpMethodWrapper httpMethodWrapper, Class<? extends RestResponse> responseClass) {
        if (result instanceof PageObject){
            PageObject<?> pageObject = (PageObject<?>) result;
            RestResponse response = AopControllerIntercept.createRestResponse(responseClass, SUCCESS.value(), true, WORK_SUCCESSFUL, pageObject.getSource(), httpMethodWrapper);
            response.setTotal(pageObject.getTotal());
            result = response;
        }
        return result;
    }
}
