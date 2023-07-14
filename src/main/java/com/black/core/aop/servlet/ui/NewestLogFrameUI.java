package com.black.core.aop.servlet.ui;

import com.black.core.aop.servlet.AopControllerIntercept;
import com.black.core.aop.servlet.HttpMethodWrapper;
import com.black.core.servlet.HttpRequestUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.StringJoiner;

/**
 * @author 李桂鹏
 * @create 2023-07-12 9:46
 */
@SuppressWarnings("all") @Log4j2
public class NewestLogFrameUI implements LogFrameUI{

    public static AnsiColor requestKeyColor = AnsiColor.BRIGHT_YELLOW;

    public static AnsiColor requestValueColor = AnsiColor.BRIGHT_WHITE;

    public static AnsiColor javaKeyColor = AnsiColor.BRIGHT_YELLOW;

    public static AnsiColor javaValueColor = AnsiColor.BRIGHT_WHITE;

    public static AnsiColor intervalColor = AnsiColor.BRIGHT_GREEN;

    public static AnsiColor TitleKeyColor = AnsiColor.BRIGHT_BLACK;

    public static String interval = "  ◤◢◤◢◤◢  ";

    public NewestLogFrameUI(){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
    }

    @Override
    public void log(HttpMethodWrapper restWrapper) throws Throwable{
        if (!log.isInfoEnabled()){
            return;
        }
        HttpServletRequest request = AopControllerIntercept.getRequest();
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
        //接口层面
        String txt = AnsiOutput.toString(AnsiColor.BRIGHT_WHITE, "\n▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂ REQUEST ▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂",
                getTitleKeyColor(), "\nSERVLET:",
                getRequestKeyColor(), "\n  请求URL: ",
                getRequestValueColor(), "{}",
                getIntervalColor(), getInterval(),
                getRequestKeyColor(), "请求方法: ",
                getRequestValueColor(), "{}",
                getIntervalColor(), getInterval(),
                getRequestKeyColor(), "请求路径: ",
                getRequestValueColor(), "{}",
                getRequestKeyColor(), "\n  客户端地址: ",
                getRequestValueColor(), "{}",
                getIntervalColor(), getInterval(),
                getRequestKeyColor(), "Content-Type: ",
                getRequestValueColor(), "{}",
                getIntervalColor(), getInterval(),
                getRequestKeyColor(), "Cookies: ",
                getRequestValueColor(), "{}",
                getTitleKeyColor(), "\nJAVA:",
                getJavaKeyColor(), "\n  控制器: ",
                getJavaValueColor(), "{}",
                getIntervalColor(), getInterval(),
                getJavaKeyColor(), "方法名: ",
                getJavaValueColor(), "{}",
                getJavaKeyColor(), "\n  参数列表: ",
                getJavaValueColor(), "{}",
                AnsiColor.BRIGHT_WHITE, "\n▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂ \tEND\t  ▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂﹏▂");
        log.info(txt, request.getRequestURL(), request.getMethod(), restWrapper.showPath(),
                HttpRequestUtil.getIpAddr(request), restWrapper.getContentType(), cookiesJoiner.toString(),
                restWrapper.getControllerClazz().getSimpleName(), restWrapper.getHttpMethod().getName(),
                restWrapper.showArgs());
    }

    AnsiColor getTitleKeyColor(){
        return TitleKeyColor;
    }

    AnsiColor getRequestKeyColor(){
        return requestKeyColor;
    }

    AnsiColor getRequestValueColor(){
        return requestValueColor;
    }

    AnsiColor getJavaKeyColor(){
        return javaKeyColor;
    }

    AnsiColor getJavaValueColor(){
        return javaValueColor;
    }

    AnsiColor getIntervalColor(){
        return intervalColor;
    }

    String getInterval(){
        return interval;
    }

    public static void main(String[] args) throws Throwable {
        new NewestLogFrameUI().log(null);
    }
}
