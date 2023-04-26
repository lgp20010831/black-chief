package com.black.core.aop.servlet.flow;

import com.black.JsonBean;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter @Setter
public class FlowMatedata extends JsonBean {

    //客户端地址
    private String clientAddress;

    //是否为代理
    private boolean proxy;

    //请求 host
    private String requestHost;

    //请求端口
    private int requestPort;

    //代理服务器地址
    private FlowProxyMatedata proxyMatedata;

    //访问总次数
    private AtomicInteger visitCount = new AtomicInteger();

    //第一次访问时间
    private String firstVisitTime;

    //最后一次访问时间
    private String lastVisitTime;

    private Map<FlowTimeUnit, RequestTimeCount> requestTimeCountMap = new ConcurrentHashMap<>();

    public int addVisitCount(){
        return visitCount.incrementAndGet();
    }

    public static void addRequest(Map<FlowTimeUnit, RequestTimeCount> requestTimeCountMap){
        for (RequestTimeCount timeCount : requestTimeCountMap.values()) {
            timeCount.request();
        }
    }

    public String showProxyString(){
        StringJoiner joiner = new StringJoiner("<---");
        addProxyString(joiner, proxyMatedata);
        return joiner.toString();
    }

    private void addProxyString(StringJoiner joiner, FlowProxyMatedata proxyMatedata){
        if (proxyMatedata != null){
            String address = proxyMatedata.getAddress();
            joiner.add("代理服务器地址: " + address);
        }
        FlowProxyMatedata proxyForMe = proxyMatedata.getProxyForMe();
        if (proxyForMe != null){
            addProxyString(joiner, proxyForMe);
        }
    }

    public String getRequestCountString(){
        if (requestTimeCountMap.isEmpty()){
            return "无数据";
        }
        StringJoiner joiner = new StringJoiner("\n");
        for (RequestTimeCount value : requestTimeCountMap.values()) {
            joiner.add(value.toString());
        }
        return joiner.toString();
    }

    public void clearRequestInfo(){
        for (RequestTimeCount timeCount : requestTimeCountMap.values()) {
            timeCount.clear();
        }
    }
}
