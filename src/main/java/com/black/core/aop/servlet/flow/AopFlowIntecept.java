package com.black.core.aop.servlet.flow;

import com.black.core.aop.AopMethodDirectAgent;
import com.black.core.aop.annotation.AopHybrid;
import com.black.core.aop.annotation.HybridSort;
import com.black.core.aop.code.AopMatchTargetClazzAndMethodMutesHandler;
import com.black.core.aop.code.AopTaskIntercepet;
import com.black.core.aop.code.AopTaskManagerHybrid;
import com.black.core.aop.code.HijackObject;
import com.black.core.aop.servlet.AopControllerIntercept;
import com.black.core.aop.servlet.ParameterWrapper;
import com.black.core.chain.GroupKeys;
import com.black.core.query.MethodWrapper;
import com.black.core.util.AnnotationUtils;
import com.black.core.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Log4j2
@AopHybrid
@HybridSort(42)
public class AopFlowIntecept implements AopTaskIntercepet, AopTaskManagerHybrid {

    private final Map<GroupKeys, FlowMonitoring> monitoringCache = new ConcurrentHashMap<>();

    @Override
    public Object processor(HijackObject hijack) throws Throwable {
        Class<?> clazz = hijack.getClazz();
        Method method = hijack.getMethod();
        HystrixsBalance annotation = getAnnotation(clazz, method);
        FlowMonitoring monitoring = null;
        if (annotation != null){
            GroupKeys groupKeys = new GroupKeys(clazz, method);
            monitoring = monitoringCache.computeIfAbsent(groupKeys, gk -> {
                FlowConfiguration configuration = new FlowConfiguration();
                AnnotationUtils.loadAttribute(annotation, configuration);
                return new FlowMonitoring(configuration);
            });
        }

        //如果当前环境是 servlet 环境 并且启用了限流
        if (monitoring != null && isRequestEnv()){
            FlowConfiguration configuration = monitoring.getConfiguration();
            Supplier<Object> supplier = configuration.getLimitResponser();
            Set<FlowTimeUnit> units = configuration.getUnits();
            FlowMatedata flowMatedata = monitoring.getClient();
            //总请求数是否需要拦截
            if (FlowMonitoring.intercept(monitoring.getInterRequestCounrMap(),
                    units, configuration.getLimitExpression(), "Interface current limiting")) {
                return supplier.get();
            }

            //用户请求数是否需要拦截
            if (FlowMonitoring.intercept(flowMatedata.getRequestTimeCountMap(),
                    units, configuration.getUserLimitExpression(), "User current limit")){
                return supplier.get();
            }
            monitoring.addClientVisit(flowMatedata);
            print(flowMatedata, monitoring);
            wiredMatedata(flowMatedata, hijack);
            FlowLocal.set(flowMatedata);
        }
        wiredTarget(hijack, monitoring);
        try {

            return hijack.doRelease(hijack.getArgs());
        }finally {
            FlowLocal.remove();
        }
    }

    private void wiredTarget(HijackObject hijack, FlowMonitoring monitoring){
        Method method = hijack.getMethod();
        MethodWrapper mw = MethodWrapper.get(method);
        Object[] args = hijack.getArgs();
        ParameterWrapper p1 = mw.getSingleParameterByType(AopFlowIntecept.class);
        if (p1 != null){
            args[p1.getIndex()] = this;
        }

        ParameterWrapper p2 = mw.getSingleParameterByType(FlowMonitoring.class);
        if(p2 != null){
            args[p2.getIndex()] = monitoring;
        }
    }

    private void wiredMatedata(FlowMatedata flowMatedata, HijackObject hijack){
        Method method = hijack.getMethod();
        MethodWrapper mw = MethodWrapper.get(method);
        Object[] args = hijack.getArgs();
        ParameterWrapper pw = mw.getSingleParameterByType(FlowMatedata.class);
        if (pw != null){
            args[pw.getIndex()] = flowMatedata;
        }
    }

    public Map<GroupKeys, FlowMonitoring> getMonitoringCache() {
        return monitoringCache;
    }

    private boolean isRequestEnv(){
        return AopControllerIntercept.getRequest() != null;
    }

    private HystrixsBalance getAnnotation(Class<?> clazz, Method method){
        HystrixsBalance annotation = method.getAnnotation(HystrixsBalance.class);
        if (annotation == null){
            annotation = clazz.getAnnotation(HystrixsBalance.class);
        }
        return annotation;
    }

    private void print(FlowMatedata matedata, FlowMonitoring monitoring){
        HttpServletRequest request = AopControllerIntercept.getRequest();
        String requestURL = request.getRequestURL().toString();
        String proxyInfo = "无代理";
        if (matedata.isProxy()) {
            proxyInfo = matedata.showProxyString();
        }
        String str = StringUtils.linkStr(AnsiOutput.toString(AnsiColor.CYAN, "访问接口地址:"),
                AnsiOutput.toString(AnsiColor.WHITE, requestURL), ", ",
                AnsiOutput.toString(AnsiColor.CYAN, "客户端真实地址:"),
                AnsiOutput.toString(AnsiColor.RED, matedata.getClientAddress()), ", ",
                AnsiOutput.toString(AnsiColor.CYAN, "代理服务器情况: "),
                AnsiOutput.toString(AnsiColor.RED, proxyInfo), "\n",
                AnsiOutput.toString(AnsiColor.CYAN, "接口总访问量: "),
                AnsiOutput.toString(AnsiColor.WHITE, "[" + monitoring.getRequestCountNum() + "]"), "\n",
                AnsiOutput.toString(AnsiColor.CYAN, "请求 host: "),
                AnsiOutput.toString(AnsiColor.RED, matedata.getRequestHost()), "\n",
                AnsiOutput.toString(AnsiColor.CYAN, "请求端口: "),
                AnsiOutput.toString(AnsiColor.BLUE, matedata.getRequestPort()), "\n",
                AnsiOutput.toString(AnsiColor.CYAN, "该用户接口历史访问量: \n"),
                AnsiOutput.toString(AnsiColor.WHITE, matedata.getRequestCountString()));
        log.info("\n{}", str);
    }



    /**
     * 提供一个匹配的东西
     */
    @Override
    public AopMatchTargetClazzAndMethodMutesHandler obtainMatcher() {
        AopMethodDirectAgent instance = AopMethodDirectAgent.getInstance();
        instance.register(this, (targetClazz, method) -> {
            return (method.isAnnotationPresent(HystrixsBalance.class) ||
                    targetClazz.isAnnotationPresent(HystrixsBalance.class)) &&
                    org.springframework.core.annotation.AnnotationUtils.getAnnotation(method, RequestMapping.class) != null;
        });
        return instance.getHandler(this);
    }

    @Override
    public AopTaskIntercepet obtainAopTaskIntercept() {
        return this;
    }
}
