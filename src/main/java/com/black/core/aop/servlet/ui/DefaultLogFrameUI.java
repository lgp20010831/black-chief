package com.black.core.aop.servlet.ui;

import com.black.core.aop.servlet.AopControllerIntercept;
import com.black.core.aop.servlet.GlobalServlet;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.servlet.HttpRequestUtil;
import com.black.core.util.CentralizedExceptionHandling;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.StringJoiner;

/**
 * @author 李桂鹏
 * @create 2023-07-12 9:44
 */
@SuppressWarnings("all") @Log4j2
public class DefaultLogFrameUI implements LogFrameUI {


    @Override
    public void log(HttpMethodWrapper restWrapper) {
        HttpServletRequest request = AopControllerIntercept.getRequest();
        try {
            if (log.isInfoEnabled()) {
                if (GlobalServlet.isEyeCatchingLog()){
                    Cookie[] cookies = request.getCookies();
                    StringJoiner cookiesJoiner = new StringJoiner(",");
                    if (cookies != null){
                        for (int i = 0; i < cookies.length; i++) {
                            Cookie cookie = cookies[i];
                            if (cookie != null){
                                cookiesJoiner.add(cookie.getName() + " = " + cookie.getValue());
                            }else {
                                cookiesJoiner.add("null");
                            }
                        }
                    }
                    log.info(AnsiOutput.toString(AnsiColor.BRIGHT_RED, "\n 接口调用 ===> 控制器: " ,
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 方法名: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 请求路径: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 请求方法: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 请求URL地址: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " Cookies: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 客户端地址: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " 参数列表: ",
                                    AnsiColor.BLUE, "{};\n",
                                    AnsiColor.BRIGHT_RED, " Content-Type: ",
                                    AnsiColor.BLUE, "{};\n"),
                            restWrapper.getControllerClazz().getSimpleName(), restWrapper.getHttpMethod().getName(),
                            restWrapper.showPath(), request.getMethod(), request.getRequestURL(),
                            cookiesJoiner.toString(), HttpRequestUtil.getIpAddr(request),
                            restWrapper.showArgs(), restWrapper.getContentType());
                }else {
                    log.info("接口调用 ===> 控制器: {};\n 方法名:{};\n 请求路径:{};\n 参数列表:{};\n Content-Type: {}",
                            restWrapper.getControllerClazz().getSimpleName(), restWrapper.getHttpMethod().getName(),
                            restWrapper.showPath(), restWrapper.showArgs(), restWrapper.getContentType());
                }
            }
        } catch (Throwable e) {
            if (log.isErrorEnabled()) {
                log.error("打印参数时发生异常");
            }
            CentralizedExceptionHandling.handlerException(e);
        }
    }
}
