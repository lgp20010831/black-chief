package com.black.core.aop.servlet.flow;

import com.black.core.aop.servlet.AopControllerIntercept;
import com.black.core.util.Assert;
import com.black.core.util.StringUtils;
import com.black.utils.ServiceUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class FlowMonitoring {

    //该接口不区分用户总的访问量
    private AtomicInteger requestCount = new AtomicInteger(0);

    private Map<FlowTimeUnit, RequestTimeCount> interRequestCounrMap = new ConcurrentHashMap<>();

    private final Map<String, FlowMatedata> matedataCache = new ConcurrentHashMap<>();

    private static final Map<String, ExpressLimitParser> parserMap = new ConcurrentHashMap<>();

    private final FlowConfiguration configuration;

    public FlowMonitoring(FlowConfiguration configuration) {
        this.configuration = configuration;
        Set<FlowTimeUnit> units = configuration.getUnits();
        if (units != null){
            for (FlowTimeUnit unit : units) {
                interRequestCounrMap.put(unit, new RequestTimeCount(unit));
            }
        }
    }

    public int getRequestCountNum() {
        return requestCount.get();
    }

    public Map<FlowTimeUnit, RequestTimeCount> getInterRequestCounrMap() {
        return interRequestCounrMap;
    }

    public FlowConfiguration getConfiguration() {
        return configuration;
    }

    public static boolean intercept(Map<FlowTimeUnit, RequestTimeCount> requestTimeCountMap,
                                    Set<FlowTimeUnit> units,
                                    Set<String> limitExpression,
                                    String msg){
        if (limitExpression != null){
            for (String express : limitExpression) {
                ExpressLimitParser limitParser = parserMap.computeIfAbsent(express, FlowMonitoring::parse);
                FlowTimeUnit unit = limitParser.unit;
                if (!units.contains(unit)){
                    continue;
                }

                RequestTimeCount timeCount = requestTimeCountMap.get(unit);
                if (timeCount != null){
                    String time = RequestTimeCount.getTime(unit);
                    if (timeCount.queryCount(time) >= limitParser.count){
                        log.info(msg + ": {}", express);
                        //限制
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static ExpressLimitParser parse(String express){
        ExpressLimitParser parser = new ExpressLimitParser();
        String[] unitAndCount = StringUtils.split(express, ":", 2, "ill format express: " + express);
        switch (unitAndCount[0]){
            case "min":
                parser.unit = FlowTimeUnit.MINUTES;
                break;
            case "second":
                parser.unit = FlowTimeUnit.SECONDS;
                break;
            case "hour":
                parser.unit = FlowTimeUnit.HOURS;
                break;
            case "day":
                parser.unit = FlowTimeUnit.DAYS;
                break;
            case "month":
                parser.unit = FlowTimeUnit.MONTHS;
                break;
            case "year":
                parser.unit = FlowTimeUnit.YEARS;
                break;
            default:
                throw new IllegalArgumentException("异常表达式单位: " + unitAndCount[0]);
        }
        parser.count = Integer.parseInt(unitAndCount[1].trim());
        return parser;
    }

    @Getter
    public static class ExpressLimitParser{
        FlowTimeUnit unit;
        int count;


    }

    public FlowMatedata getClient(){
        HttpServletRequest request = AopControllerIntercept.getRequest();
        String clientAddress = getClientAddress(request);
        FlowMatedata flowMatedata = matedataCache.computeIfAbsent(clientAddress, ca -> {
            FlowMatedata matedata = new FlowMatedata();
            matedata.setClientAddress(ca);
            matedata.setFirstVisitTime(ServiceUtils.now());
            Map<FlowTimeUnit, RequestTimeCount> requestTimeCountMap = matedata.getRequestTimeCountMap();
            Set<FlowTimeUnit> units = configuration.getUnits();
            if (units != null){
                for (FlowTimeUnit unit : units) {
                    requestTimeCountMap.put(unit, new RequestTimeCount(unit));
                }
            }
            return matedata;
        });
        flowMatedata.setRequestHost(request.getRemoteHost());
        flowMatedata.setRequestPort(request.getRemotePort());
        processProxy(request, flowMatedata);
        return flowMatedata;
    }

    public void addClientVisit(FlowMatedata flowMatedata){
        requestCount.incrementAndGet();
        flowMatedata.setLastVisitTime(ServiceUtils.now());
        flowMatedata.addVisitCount();
        //增加指定时间单位内的请求数量
        FlowMatedata.addRequest(flowMatedata.getRequestTimeCountMap());
        FlowMatedata.addRequest(interRequestCounrMap);

    }

    public static void processProxy(HttpServletRequest request, FlowMatedata flowMatedata){
        String forwardHeader = request.getHeader("X-Forwarded-For");
        if (forwardHeader == null){
            flowMatedata.setProxy(false);
            return;
        }
        try {

            String[] addresses = forwardHeader.split(",");
            FlowProxyMatedata proxy = new FlowProxyMatedata();
            FlowProxyMatedata current = null;
            for (int i = 1; i < addresses.length; i++) {
                String proxyAddress = addresses[i];
                if (current == null){
                    current = proxy;
                    current.setAddress(proxyAddress);
                }else {
                    FlowProxyMatedata flowProxyMatedata = new FlowProxyMatedata();
                    flowProxyMatedata.setAddress(proxyAddress);
                    current.setProxyForMe(flowProxyMatedata);
                    current = flowProxyMatedata;
                }
            }
            flowMatedata.setProxy(true);
            flowMatedata.setProxyMatedata(proxy);
        }catch (Throwable e){
            log.warn("can not parse proxy address");
        }
    }

    public static String getClientAddress(HttpServletRequest request){
        String forwardHeader = request.getHeader("X-Forwarded-For");
        String reallyClientAddress = null;
        if (forwardHeader != null){
            String[] addresses = forwardHeader.split(",");
            if (addresses.length > 0){
                reallyClientAddress = addresses[0];
            }
        }

        if (reallyClientAddress == null){
            reallyClientAddress = request.getRemoteAddr();
        }

        Assert.notNull(reallyClientAddress, "unknown client address:" + request);
        return reallyClientAddress;
    }
}
