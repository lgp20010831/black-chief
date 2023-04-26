package com.black.core.mvc.response;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Log4j2
public class SupportController {

    public static final String PAGE_SIZE = "pageSize";

    public static final String PAGE_NUM = "pageNum";

    public static final String JSON_REQUEST_TYPE = "application/json";

    public static final String FORM_DATA_REQUEST_TYPE = " application/x-www-form-urlencoded";

    public static final String PART_REQUEST_TYPE = "application/octet-stream";
    protected HttpServletRequest getRequest(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getRequest();
    }

    protected void page(){
        page(null);
    }

    protected void page(JSONObject body){
        if (body == null){
            pageOfFormData();
        }
        if (body.containsKey(PAGE_SIZE) || body.containsKey(PAGE_NUM)){
            return;
        }
        doPage(body.get(PAGE_SIZE), body.get(PAGE_NUM));
    }

    protected void pageOfFormData(){
        final HttpServletRequest request = getRequest();
        if (request == null){
            if (log.isInfoEnabled()) {
                log.error("无法获取请求对象实例, 请保持当前线程与请求线程一致");
                return;
            }
        }
        doPage(request.getParameter(PAGE_SIZE), request.getParameter(PAGE_NUM));
    }

    protected void doPage(Object pageSize, Object pageNum){
        if (pageSize == null || pageNum == null){
            return;
        }
        PageHelper.startPage(Integer.parseInt(pageSize.toString()), Integer.parseInt(pageNum.toString()));
    }
}
