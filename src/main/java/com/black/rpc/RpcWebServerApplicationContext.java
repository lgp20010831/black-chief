package com.black.rpc;

import com.black.nio.code.Configuration;
import com.black.nio.code.NioServerContext;
import com.black.rpc.annotation.Actuator;
import com.black.rpc.socket.NioServerSocketTemplate;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.vfs.VFS;
import com.black.vfs.VfsScanner;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public class RpcWebServerApplicationContext extends RpcWebApplicationContext{

    private final VfsScanner scanner;

    private final RpcMethodRegister methodRegister;

    private NioServerContext serverContext;

    private Consumer<Configuration> nioConfigurationHook;

    public RpcWebServerApplicationContext(@NonNull RpcConfiguration configuration) {
        super(configuration);
        methodRegister = new RpcMethodRegister(configuration);
        configuration.setRpcMessageResolver(new RequestMessageResolver(configuration));
        scanner = VFS.findVfsScanner();
    }

    public RpcMethodRegister getMethodRegister() {
        return methodRegister;
    }

    @Override
    public void shutdown() {
        if (serverContext != null){
            serverContext.shutdownNow();
            serverContext = null;
        }
    }

    public void bind() throws IOException {
        if (serverContext != null) return;
        InetSocketAddress address = rpcConfiguration.getAddress();
        Configuration configuration = new Configuration();
        configuration.setPort(address.getPort());
        configuration.setHost(address.getHostName());
        configuration.setOpenWorkPool(true);
        configuration.setWriteInCurrentLoop(false);
        configuration.setChannelInitialization(pipeline -> {
            pipeline.addLast(new NioServerSocketTemplate(rpcConfiguration));
        });
        if (nioConfigurationHook != null){
            nioConfigurationHook.accept(configuration);
        }
        serverContext = new NioServerContext(configuration);
        serverContext.start();
    }

    public void scanAction(String packageName){
        Set<Class<?>> classes = scanner.load(packageName);
        RpcMethodRegister methodRegister = getMethodRegister();
        for (Class<?> type : classes) {
            if (BeanUtil.isSolidClass(type)){
                ClassWrapper<?> cw = ClassWrapper.get(type);
                boolean actionClass = AnnotationUtils.getAnnotation(cw.get(), Actuator.class) != null;
                for (MethodWrapper mw : cw.getMethods()) {
                    if (actionClass || AnnotationUtils.getAnnotation(mw.getMethod(), Actuator.class) != null){
                        methodRegister.registerMethodInvoker(mw, cw);
                    }
                }
            }
        }
    }

    public void registerAction(Object action){
        RpcMethodRegister methodRegister = getMethodRegister();
        Class<Object> primordialClass = BeanUtil.getPrimordialClass(action);
        ClassWrapper<Object> acw = ClassWrapper.get(primordialClass);
        Collection<MethodWrapper> methods = acw.getMethods();
        for (MethodWrapper mw : methods) {
            Method rawMethod = mw.getMethod();
            if (!Modifier.isPrivate(rawMethod.getModifiers()) ||
            AnnotationUtils.getAnnotation(rawMethod, Actuator.class) != null){
                methodRegister.registerMethodInvoker(mw, action);
            }
        }
    }

    public void setNioConfigurationHook(Consumer<Configuration> nioConfigurationHook) {
        this.nioConfigurationHook = nioConfigurationHook;
    }
}
