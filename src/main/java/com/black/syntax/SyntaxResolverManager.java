package com.black.syntax;

import com.black.throwable.ParserTxtException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@SuppressWarnings("all")
public class SyntaxResolverManager {

    private static Collection<SyntaxExpressionResolver> resolvers;

    private static Collection<SyntaxInterlocutor> interlocutors;

    private static SyntaxExpressionResolver defaultResolver;

    private SyntaxExpressionResolver instanceDefaultResolver;

    private final Collection<SyntaxExpressionResolver> privateResolvers = new LinkedBlockingQueue<>();

    private final Collection<SyntaxInterlocutor> privateInterlocutor = new LinkedBlockingQueue<>();

    private static final Map<String, SyntaxResolverManager> cache = new ConcurrentHashMap<>();

    private static void init(){
        if (resolvers == null){
            resolvers = new LinkedBlockingQueue<>();
            interlocutors = new LinkedBlockingQueue<>();
            registerResolver(new AviatorSyntaxResolver());
            registerResolver(new JSONSyntaxResolver());
            defaultResolver = new DefaultSyntaxResolver();
        }
    }

    public static SyntaxResolverManager instance(String name){
        return instance(name,  null);
    }

    public static SyntaxResolverManager instance(String name, Consumer<SyntaxResolverManager> callback){
        return cache.computeIfAbsent(name, n -> {
            SyntaxResolverManager manager = new SyntaxResolverManager();
            manager.setInstanceDefaultResolver(defaultResolver);
            if (callback != null){
                callback.accept(manager);
            }
            return manager;
        });
    }

    public static Collection<SyntaxInterlocutor> getInterlocutors() {
        return interlocutors;
    }

    public static Collection<SyntaxExpressionResolver> getResolvers() {
        return resolvers;
    }

    public static void registerResolver(SyntaxExpressionResolver resolver){
        init();
        Collection<SyntaxExpressionResolver> resolvers = getResolvers();
        if (resolver != null){
            resolvers.add(resolver);
        }
    }

    public static void registerInterlocutors(SyntaxInterlocutor interlocutor){
        init();
        Collection<SyntaxInterlocutor> interlocutors = getInterlocutors();
        if (interlocutor != null){
            interlocutors.add(interlocutor);
        }
    }

    public static void setDefaultResolver(SyntaxExpressionResolver defaultResolver) {
        SyntaxResolverManager.defaultResolver = defaultResolver;
    }

    public static Object resolverItem(String item, Map<String, Object> source, SyntaxMetadataListener syntaxMetadataListener){
        init();
        if (item == null){
            return null;
        }
        Collection<SyntaxExpressionResolver> resolvers = getResolvers();
        SyntaxExpressionResolver targetResolver = null;
        for (SyntaxExpressionResolver resolver : resolvers) {
            if (resolver.supportType(item)) {
                targetResolver = resolver;
                break;
            }
        }
        if (targetResolver == null){
            targetResolver = defaultResolver;
        }
        item = targetResolver.cutItem(item);
        try{
            Object result =  targetResolver.resolver(item, source, syntaxMetadataListener);
            Collection<SyntaxInterlocutor> interlocutors = getInterlocutors();
            for (SyntaxInterlocutor interlocutor : interlocutors) {
                interlocutor.interlude(item, source, result);
            }
            return result;
        }catch (Throwable e){
            throw new ParserTxtException("parse txt: " + item + ", has ill", e);
        }
    }

    public void registerPrivateResolver(SyntaxExpressionResolver resolver){
        privateResolvers.add(resolver);
    }

    public void registerPrivateInterlocutor(SyntaxInterlocutor interlocutor){
        privateInterlocutor.add(interlocutor);
    }

    public void setInstanceDefaultResolver(SyntaxExpressionResolver instanceDefaultResolver) {
        this.instanceDefaultResolver = instanceDefaultResolver;
    }

    public Object resolve0(String item, Map<String, Object> source, SyntaxMetadataListener syntaxMetadataListener){
        init();
        if (item == null){
            return null;
        }
        ArrayList<SyntaxExpressionResolver> list = new ArrayList<>(resolvers);
        list.addAll(privateResolvers);
        SyntaxExpressionResolver targetResolver = null;
        for (SyntaxExpressionResolver resolver : list) {
            if (resolver.supportType(item)) {
                targetResolver = resolver;
                break;
            }
        }
        if (targetResolver == null){
            targetResolver = instanceDefaultResolver;
        }
        item = targetResolver.cutItem(item);
        try{
            Object result =  targetResolver.resolver(item, source, syntaxMetadataListener);
            ArrayList<SyntaxInterlocutor> interlocutors = new ArrayList<>(getInterlocutors());
            interlocutors.addAll(privateInterlocutor);
            for (SyntaxInterlocutor interlocutor : interlocutors) {
                interlocutor.interlude(item, source, result);
            }
            return result;
        }catch (Throwable e){
            throw new ParserTxtException("parse txt: " + item + ", has ill", e);
        }
    }

}
