package com.black.core.sql.code.page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.black.page.PageCache;
import com.black.core.aop.code.HijackObject;
import com.black.core.aop.servlet.*;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.OpenSqlPage;
import com.black.core.util.Convert;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestBody;


import static com.black.core.response.Code.SUCCESS;
import static com.black.core.response.VariablePool.WORK_SUCCESSFUL;

@Log4j2
@GlobalAround
public class PageMethodAround implements GlobalAroundResolver {

    @Override
    public Object[] handlerArgs(Object[] args, HttpMethodWrapper httpMethodWrapper) {
        MethodWrapper methodWrapper = httpMethodWrapper.getMethodWrapper();
        OpenSqlPage sqlPage = methodWrapper.getAnnotation(OpenSqlPage.class);
        if (sqlPage != null){
            Integer pageSize = null, pageNum = null;
            if (httpMethodWrapper.isJsonRequest()) {
                ParameterWrapper parameter = httpMethodWrapper.getSinglonParameterByAnnotation(RequestBody.class);
                if (parameter == null){
                    log.warn("无法找到 json 请求体, 无法进行分页");
                    return args;
                }
                Object arg = args[parameter.getIndex()];
                if (arg != null){
                    JSONObject body;
                    if (arg instanceof JSONObject) {
                        body = (JSONObject) arg;
                    } else {
                        body = JSON.parseObject(arg.toString());
                    }
                    pageNum = body.getInteger(sqlPage.pageNum());
                    pageSize = body.getInteger(sqlPage.pageSize());
                }
            }else {
                pageSize = Convert.toInt(AopControllerIntercept.getRequest().getParameter(sqlPage.pageSize()));
                pageNum = Convert.toInt(AopControllerIntercept.getRequest().getParameter(sqlPage.pageNum()));
            }
            if (pageSize == null || pageNum == null) {
                return args;
            }
            doStartPage(pageSize, pageNum);
        }
        return args;
    }

    @Override
    public Object handlerException(Throwable e, Class<? extends RestResponse> responseClass, HttpMethodWrapper mw) throws Throwable {
        PageCache.remove();
        PageManager.close();
        return GlobalAroundResolver.super.handlerException(e, responseClass, mw);
    }

    protected void doStartPage(Integer pageSize, Integer pageNum){
        Page<?> page = PageHelper.openPage(pageNum, pageSize);
        PageCache.setPage(page);
    }

    @Override
    public Object handlerAfterInvoker(Object result, HttpMethodWrapper httpMethodWrapper, Class<? extends RestResponse> responseClass) {

        Page<?> page = PageCache.getPage();
        if (page != null){
            try {

                RestResponse response;
                if (result instanceof RestResponse){
                    response = (RestResponse) result;
                }else {
                    response = AopControllerIntercept.createRestResponse(responseClass, SUCCESS.value(), true, WORK_SUCCESSFUL, result, httpMethodWrapper);
                }
                response.setTotal((long) page.getTotal());
                return response;
            }finally {
                PageCache.remove();
                PageManager.close();
            }
        }
        return result;
    }

    @Override
    public Object interceptCallBack(HijackObject hijack, Object[] args, HttpMethodWrapper httpMethodWrapper, Object chainResult) {
        if (PageCache.getPage() != null){
            PageCache.remove();
            PageManager.close();
        }
        return chainResult;
    }
}
