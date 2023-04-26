package com.black.core.spring.component;

import com.black.core.cache.ClassSourceCache;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.driver.ApplicationDriver;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ClearApplicationCache implements ApplicationDriver {

    @Override
    public void whenApplicationStop(ChiefExpansivelyApplication application) {
        if (log.isInfoEnabled()) {
            log.info("clear application cache");
        }
        ClassSourceCache.getSourceCache().clear();
        clearCache(application);
    }
}
