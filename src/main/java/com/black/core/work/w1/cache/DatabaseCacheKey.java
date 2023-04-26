package com.black.core.work.w1.cache;


import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.work.w1.TaskFlowQueue;
import com.black.core.work.w1.TaskGlobalListener;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
public class DatabaseCacheKey implements CacheTask {

    @Setter DataSource dataSource;
    //主要数据源
    @Setter SqlSessionFactory sqlSessionFactory;
    TaskMapper taskMapper;
    public DatabaseCacheKey(BeanFactory beanFactory){
        try {
            this.dataSource = beanFactory.getBean(DataSource.class);
            this.sqlSessionFactory = beanFactory.getBean(SqlSessionFactory.class);
        }catch (BeansException be){
            if (log.isErrorEnabled()) {
                log.error("can not get datasource or sqlSessionFactory");
            }
            return;
        }
        checkTable();
    }

    //创建表
    //然后读取表里的内容
    protected void checkTable(){
        try {
            PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
            Resource resource = patternResolver.getResource("task-sql/task-queue.sql");
            Connection connection = DataSourceUtils.getConnection(dataSource);
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.runScript(new InputStreamReader(resource.getInputStream()));
            connection.close();
        }catch (Throwable e){
            CentralizedExceptionHandling.handlerException(e);
        }
        Configuration configuration = sqlSessionFactory.getConfiguration();
        configuration.addMapper(TaskMapper.class);
        taskMapper = configuration.getMapper(TaskMapper.class, sqlSessionFactory.openSession(ExecutorType.SIMPLE));
    }



    @Override
    public void writeCache(TaskGlobalListener taskGlobalListener) {
        taskGlobalListener.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        DatabaseEntry databaseEntry = new DatabaseEntry(taskGlobalListener);
        DatabaseEntry saveEntry = taskMapper.selectById(databaseEntry.getTaskId());
        if (saveEntry != null){

            //更新
            if (taskMapper.updateEntry(databaseEntry)) {
                if (log.isInfoEnabled()) {
                    log.info("update task info to database");
                }
            }
        }else {

            //添加
            if (taskMapper.insert(databaseEntry)) {
                if (log.isInfoEnabled()) {
                    log.info("write task info to database");
                }
            }
        }
    }

    @Override
    public TaskGlobalListener readTaskListener(String taskId) {
        DatabaseEntry databaseEntry = taskMapper.selectById(taskId);
        if (databaseEntry != null){
            return databaseEntry.convert();
        }
        return null;
    }

    @Override
    public void finishTask(TaskGlobalListener listener, boolean result) {
        taskMapper.remove(listener.getTaskId());
        taskMapper.writeHistory(new InstanceHistory(listener, String.valueOf(result)));
    }

    @Override
    public void processorCloseServer(Long life, Long futureStart, Long serverStopTime, String taskId) {
        taskMapper.whenClose(life, futureStart, serverStopTime, taskId);
    }

    @Override
    public void init(TaskFlowQueue queue) {
        List<DatabaseEntry> databaseEntries = taskMapper.selectAllEntries(queue.alias());
        //将收集到的 entry 转换成 listener
        List<TaskGlobalListener> listeners = databaseEntries
                .stream()
                .map(DatabaseEntry::convert)
                .collect(Collectors.toList());
        for (TaskGlobalListener listener : listeners) {

            //listener两种情况, 一种是不是定时的
            //另一种就是定时, 任务为完成
            Long serverStopTime = listener.serverStopTime();
            Long futureStart = listener.futureStart();
            Long life = listener.life();
            long nowTime = System.currentTimeMillis();
            if (futureStart != null && life != null && serverStopTime != null){
                long taskLife = life - (nowTime - futureStart);
                if (log.isInfoEnabled()) {
                    log.info("recovery task: {} life :{}", listener.getTaskId(), taskLife);
                }
                //提交任务
                queue.submitTimerTask(listener, TimeUnit.MILLISECONDS, taskLife);
            }else {

                //不是定时任务处理
                if (log.isInfoEnabled()) {
                    log.info("task :{} is not timer task", listener.getTaskId());
                }
                //process...
                queue.serverBraking(listener);
            }
        }
    }
}
