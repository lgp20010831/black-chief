package com.black.nio.group;

import com.black.bin.ApplyProxyFactory;
import com.black.core.log.CommonLog4jLog;
import com.black.core.log.IoLog;
import com.black.core.util.Assert;
import lombok.Getter;
import lombok.Setter;


@Setter @Getter
public class Configuration {

    private boolean supportChain = false;

    private GioResolver singleResolver;

    private GioChainResolver headChainResolver;

    private GioChainResolver lastChainResolver;

    private String host = "0.0.0.0";

    private int port = 3333;

    private IoLog log = new CommonLog4jLog();

    private final NioType nioType;

    private int ioThreadNum = 10;

    public Configuration(NioType nioType) {
        this.nioType = nioType;
    }

    public GioResolver getIntelligentResolver(){
        GioResolver resolver;
        if (supportChain){
            resolver = headChainResolver;
        }else {
            resolver = singleResolver;
        }
        Assert.notNull(resolver, "not find gio resolver");
        return resolver;
    }

    private void addChainResolver0(GioChainResolver resolver){
        if (!(resolver instanceof GioChainResolver)){
            throw new IllegalStateException("chain model, resolver must is chain resolver");
        }
        GioChainResolverWrapper resolverWrapper = new GioChainResolverWrapper(resolver);
        resolverWrapper = ApplyProxyFactory.proxy(resolverWrapper, (args, method, beanClass, template) -> {
            String name = method.getName();
            if ("fire".equals(name) && method.getParameterCount() == 0){
                GioChainResolverWrapper bean = (GioChainResolverWrapper) template.getBean();
                GioChainResolverWrapper next = bean.getNext();
                if (next != null){
                    return method.invoke(next, args);
                }
            }else {
                return template.invokeOriginal(args);
            }
            return null;
        });
        if (headChainResolver == null){
            headChainResolver = resolverWrapper;
        }

        if (lastChainResolver != null){
            ((GioChainResolverWrapper)lastChainResolver).setNext(resolverWrapper);

        }
        lastChainResolver = resolverWrapper;
    }

    public void addChainResolver(GioChainResolver resolver){
        if (supportChain){
            addChainResolver0(resolver);
        }else {
            singleResolver = resolver;
        }
    }

    public void addResolver(GioResolver gioResolver){
        if (supportChain){
            addChainResolver0((GioChainResolver) gioResolver);
        }else {
            singleResolver = gioResolver;
        }
    }
}
