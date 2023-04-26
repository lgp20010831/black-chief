package com.black.core.aop.servlet.security;

import com.black.core.aop.servlet.AopControllerIntercept;
import com.black.core.aop.servlet.HttpMethodWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import javax.servlet.http.HttpServletRequest;

@Log4j2
public class UrlSecutityHandler implements SecurityHandler{

    @Override
    public boolean doIntercept(Object[] args, HttpMethodWrapper mw) {
        HttpServletRequest request = AopControllerIntercept.getRequest();
        String requestUrl = request.getRequestURL().toString();
        if (log.isInfoEnabled()) {
            log.info("secutity manager processor current request url: [{}]",
                    AnsiOutput.toString(AnsiColor.GREEN, requestUrl));
        }

        String servletPath = request.getServletPath();
        if (log.isInfoEnabled()) {
            log.info("secutity manager processor current servlet path: [{}]",
                    AnsiOutput.toString(AnsiColor.GREEN, servletPath));
        }

        return false;
    }
}
