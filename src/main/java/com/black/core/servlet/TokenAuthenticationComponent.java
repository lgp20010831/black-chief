package com.black.core.servlet;

import com.black.core.servlet.annotation.EnableTokenValidator;
import com.black.core.servlet.intercept.ServletInterceptTaskChain;
import com.black.core.servlet.token.PostTokenValidatorHandler;
import com.black.core.servlet.token.TokenIntercept;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.ChooseScanRangeHolder;
import com.black.core.spring.EnabledControlRisePotential;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LoadSort;
import com.black.holder.SpringHodler;
import com.black.utils.NameUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;

@Log4j2
@LoadSort(30)
public class TokenAuthenticationComponent implements OpenComponent, EnabledControlRisePotential {

    private String tokenHeader;

    private String tokenPrefix;

    private ValidatorToken validatorToken;

    private TokenResolver validator;

    private final Collection<String> validatorRange = new HashSet<>();

    private static boolean enableToken = false;

    PostTokenValidatorHandler postTokenValidatorHandler;

    public static boolean isEnableToken(){
        return enableToken;
    }

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) {
        try {
            enableToken = true;
            Collection<Object> configurationMutes = expansivelyApplication.getApplicationConfigurationMutes();
            for (Object mute : configurationMutes) {
                if (mute instanceof ValidatorTokenConfigurer){
                    ValidatorTokenConfigurer tokenConfigurer = (ValidatorTokenConfigurer) mute;
                    handlerConfig(tokenConfigurer);
                }
            }
        }finally {
            if (validatorRange.isEmpty()) {
                validatorRange.addAll(AutoConfigurationPackages.get(SpringHodler.getApplicationContext()));
            }
        }

        instanceValidator();
        SpringHodler.registerSpringBean(NameUtil.getName(validator), validator);
        ServletInterceptTaskChain interceptTaskChain = expansivelyApplication.queryComponent(ServletInterceptTaskChain.class);
        if (interceptTaskChain != null){
            interceptTaskChain.registerInterceptor(createInterceptor(validator, postTokenValidatorHandler, validatorRange));
        }else {
            if (log.isDebugEnabled()) {
                log.debug("查找不到组件 ServletInterceptTaskChain 无法添加拦截器");
            }
        }
    }

    protected void instanceValidator(){
        validator = new TokenValidator(tokenHeader, tokenPrefix);
        validator.setValidatorToken(validatorToken);
    }

    protected HandlerInterceptor createInterceptor(TokenResolver resolver,
                                                   PostTokenValidatorHandler postTokenValidatorHandler,
                                                   Collection<String> validatorRange){
        return new TokenIntercept(resolver, postTokenValidatorHandler, validatorRange);
    }

    protected void handlerConfig(ValidatorTokenConfigurer configurer){
        setTokenHeader(configurer.tokenHeaderString());
        setTokenPrefix(configurer.tokenPrefix());
        ValidatorToken validatorToken = configurer.giveParser();
        if (validatorToken != null){
            if (this.validatorToken != null){
                if (log.isErrorEnabled()) {
                    log.error("token 验证的处理器必需唯一");
                }
                throw new RuntimeException("token 验证处理器不唯一, 已经存在的验证器: " + this.validatorToken);
            }
            setValidatorToken(validatorToken);
        }

        PostTokenValidatorHandler postTokenValidatorHandler = configurer.giveTokenHandler();
        if (postTokenValidatorHandler != null){
            if (this.postTokenValidatorHandler != null){
                if (log.isErrorEnabled()) {
                    log.error("token 验证异常的处理器必需唯一");
                }
                throw new RuntimeException("token 验证异常处理器不唯一, 已经存在的处理器:" + this.postTokenValidatorHandler);
            }
            setPostTokenValidatorHandler(postTokenValidatorHandler);
        }
        String[] range = configurer.validatorRange();
        if (range != null || range.length != 0){
            ChooseScanRangeHolder.filterVaildRange(validatorRange, range);
        }
    }

    public void setPostTokenValidatorHandler(PostTokenValidatorHandler postTokenValidatorHandler) {
        this.postTokenValidatorHandler = postTokenValidatorHandler;
    }

    public void setTokenHeader(String tokenHeader) {
        this.tokenHeader = tokenHeader;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public void setValidatorToken(ValidatorToken validatorToken) {
        this.validatorToken = validatorToken;
    }

    @Override
    public Class<? extends Annotation> registerEnableAnnotation() {
        return EnableTokenValidator.class;
    }
}
