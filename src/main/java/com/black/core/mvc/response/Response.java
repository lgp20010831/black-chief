package com.black.core.mvc.response;

import com.black.api.ResponseData;
import com.black.core.aop.servlet.RestResponse;
import com.black.core.json.Trust;
import io.swagger.annotations.ApiModelProperty;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@ToString
@NoArgsConstructor
public final class Response implements Serializable, RestResponse {

    public static boolean printErrorStack = true;

    /** 响应码 */
    @ApiModelProperty("响应code")
    Integer code;
    /** 响应体 */
    @ResponseData
    @ApiModelProperty("响应结果")
    Object result;
    /** 响应信息 **/
    @ApiModelProperty("响应信息")
    String message;
    /** 响应是否成功 **/
    @ApiModelProperty("是否响应成功")
    Boolean successful;
    /** 数据总量 */
    @ApiModelProperty("如果该接口为分页查询, 则显示总数据量")
    Long total;
    /* 异常栈 */
    @ApiModelProperty("当程序发生异常时显示异常栈")
    String errorStack;

    public Integer getCode()        {return code;}
    public Object getResult()       {return result;}
    public String getMessage()      {return message;}
    public Boolean getSuccessful()  {return successful;}
    public Long getTotal()       {return total;}
    public String getErrorStack() {
        return errorStack;
    }

    public void setCode(Integer code)                   {this.code = code;}
    public void setSuccessful(Boolean successful)       {this.successful = successful;}
    public void setResult(Object result)                {this.result = result;}
    public void setMessage(String message)              {this.message = message;}
    public void setTotal(Long total)                 {this.total = total;}

    public static Response fair(String msg){
        return fair(msg, 500);
    }

    public static Response fair(String msg, Integer code){
        return fair(msg, code, null);
    }

    @Trust
    public static Response fair(String msg, Integer code, Object result){
        Response response = new Response();
        response.setSuccessful(false);
        response.setResult(result);
        response.setMessage(msg);
        response.setCode(code);
        return response;
    }

    @Trust
    public static Response ok(Object result){
        Response response = new Response();
        response.setSuccessful(true);
        response.setResult(result);
        response.setCode(200);
        response.setMessage("操作成功");
        return response;
    }

    public static void closeStack(){
        printErrorStack = false;
    }

    @Override
    public void setThrowableStackTrace(String msg) {
        if (printErrorStack){
            errorStack = msg;
        }
    }

    @Override
    public boolean enabledThrowableStack() {
        return true;
    }

    @Override
    public Object obtainResult() {
        return result;
    }

    @Override
    public Long obtainTotal() {
        return total;
    }

    public Response(Integer code, Boolean successful, String message, Object result) {
        this.code = code;
        this.result = result;
        this.message = message;
        this.successful = successful;
    }

    public Response(Integer code,  Boolean successful, String message) {
        this.code = code;
        this.message = message;
        this.successful = successful;
    }

    public Response(Integer code, Object result, String message, Boolean successful, Long total) {
        this.code = code;
        this.result = result;
        this.message = message;
        this.successful = successful;
        this.total = total;
    }
}
