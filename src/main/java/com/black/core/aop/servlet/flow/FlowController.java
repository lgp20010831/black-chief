package com.black.core.aop.servlet.flow;

import com.black.role.SkipVerification;
import com.black.core.chain.GroupKeys;
import com.black.core.servlet.annotation.UnwantedVerify;
import com.black.utils.ServiceUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@UnwantedVerify
@SkipVerification
@RestController
@CrossOrigin @HystrixsBalance
public class FlowController {

    @GetMapping("clearFlow")
    public Object clear(AopFlowIntecept intecept){
        log.info("clear flow cache");
        Map<GroupKeys, FlowMonitoring> monitoringCache = intecept.getMonitoringCache();
        int size = 0;
        synchronized (intecept.getMonitoringCache()){
            for (FlowMonitoring monitoring : monitoringCache.values()) {
                Map<FlowTimeUnit, RequestTimeCount> interRequestCounrMap = monitoring.getInterRequestCounrMap();
                for (RequestTimeCount timeCount : interRequestCounrMap.values()) {
                    Map<String, AtomicInteger> countMap = timeCount.getCountMap();
                    size += countMap.size();
                    countMap.clear();
                }
            }
        }
        return ServiceUtils.ofMap("before clear", size, "after clear", 0);
    }



}
