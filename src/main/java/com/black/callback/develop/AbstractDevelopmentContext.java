package com.black.callback.develop;

import com.black.core.tools.BeanUtil;
import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.util.StreamUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author 李桂鹏
 * @create 2023-05-17 14:00
 */
@SuppressWarnings("all") @Log4j2
public abstract class AbstractDevelopmentContext implements DevelopmentContext{

    private final LinkedBlockingQueue<Developer> developers = new LinkedBlockingQueue<>();

    public AbstractDevelopmentContext(){
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
        Runtime.getRuntime().addShutdownHook(new Thread((this::shutdown)));
    }

    @Override
    public void shutdown() {
        for (Developer developer : developers) {
            try {
                developer.shutdown(this);
            }catch (Throwable ex){
                CentralizedExceptionHandling.handlerException(ex);
                error("component shutdown exception occurred");
            }
        }
    }

    protected void error(String msg, Object... params){
        log.error(AnsiOutput.toString(AnsiColor.RED, "[develop] ==> " + msg), params);
    }

    public void registerDeveloper(Developer developer){
        List<DevelopmentFilter> filters = getParticularDeveloper(DevelopmentFilter.class);
        for (DevelopmentFilter filter : filters) {
            if (filter.intercept(this, developer)) {
                try {
                    developer.rejected(this, (Developer) filter);
                }catch (Throwable e){
                    CentralizedExceptionHandling.handlerException(e);
                    error("component rejected exception occurred");
                    if (developer.attachThrowable()){
                        throw new DeveloperException(e);
                    }
                }

                return;
            }
        }

        try {
            developer.registered(this);
        }catch (Throwable e){
            CentralizedExceptionHandling.handlerException(e);
            error("component registered exception occurred");
            if (developer.attachThrowable()){
                throw new DeveloperException(e);
            }
        }

        register0(developer);
    }

    protected void register0(Developer developer){
        developers.add(developer);
    }

    @Override
    public <T> List<T> getParticularDeveloper(Class<T> type) {
        Object source = StreamUtils.filterList(developers, developer -> {
            Class<Developer> developerClass = BeanUtil.getPrimordialClass(developer);
            return type.isAssignableFrom(developerClass);
        });
        return (List<T>) source;
    }


    @Override
    public void postSources(Collection<Class<?>> source) {
        List<RecordDeveloper> recordDevelopers = getParticularDeveloper(RecordDeveloper.class);
        if (recordDevelopers != null){
            for (Class<?> clazz : source) {
                for (RecordDeveloper developer : recordDevelopers) {
                    try {
                        developer.record(this, clazz);
                    }catch (Throwable e){
                        CentralizedExceptionHandling.handlerException(e);
                        error("component record exception occurred");
                        if (((Developer)developer).attachThrowable()){
                            throw new DeveloperException(e);
                        }
                    }

                }
            }
            postSourceFinish();
        }

    }

    protected void postSourceFinish(){
        List<RecordDeveloper> recordDevelopers = getParticularDeveloper(RecordDeveloper.class);
        for (RecordDeveloper developer : recordDevelopers) {
            try {
                developer.finish();
            }catch (Throwable e){
                CentralizedExceptionHandling.handlerException(e);
                error("component finish exception occurred");
                if (((Developer)developer).attachThrowable()){
                    throw new DeveloperException(e);
                }
            }
        }
    }

    @Override
    public void prepareLoad() {
        for (Developer developer : developers) {
            try {
                developer.applicationPrepareLoad(this);
            }catch (Throwable e){
                CentralizedExceptionHandling.handlerException(e);
                error("component applicationPrepareLoad exception occurred");
                if (((Developer)developer).attachThrowable()){
                    throw new DeveloperException(e);
                }
            }
        }
    }

    @Override
    public void failed() {
        for (Developer developer : developers) {
            try {
                developer.applicationFailed(this);
            }catch (Throwable e){
                CentralizedExceptionHandling.handlerException(e);
                error("component applicationFailed exception occurred");
                if (((Developer)developer).attachThrowable()){
                    throw new DeveloperException(e);
                }
            }
        }
    }

    @Override
    public void running() {
        for (Developer developer : developers) {
            try {
                developer.applicationRunning(this);
            }catch (Throwable e){
                CentralizedExceptionHandling.handlerException(e);
                error("component applicationRunning exception occurred");
                if (((Developer)developer).attachThrowable()){
                    throw new DeveloperException(e);
                }
            }
        }
    }

    @Override
    public void started() {
        for (Developer developer : developers) {
            try {
                developer.applicationStarted(this);
            }catch (Throwable e){
                CentralizedExceptionHandling.handlerException(e);
                error("component applicationStarted exception occurred");
                if (((Developer)developer).attachThrowable()){
                    throw new DeveloperException(e);
                }
            }
        }
    }
}
