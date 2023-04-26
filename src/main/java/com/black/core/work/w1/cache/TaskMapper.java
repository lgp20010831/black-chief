package com.black.core.work.w1.cache;

import org.apache.ibatis.annotations.*;

import java.util.List;

@SuppressWarnings("all")
public interface TaskMapper {

    @Insert("insert into task_queue values (#{dbe.taskId}, #{dbe.size}, #{dbe.currentIndex}, #{dbe.key}, #{dbe.jsonString}, " +
            "#{dbe.startTime}, #{dbe.updateTime}, #{dbe.alias}, #{dbe.serverStopTime}, #{dbe.futureStart}, #{dbe.life})")
    boolean insert(@Param("dbe") DatabaseEntry entry);

    @Select("select * from task_queue where \"taskId\" = #{taskId}")
    DatabaseEntry selectById(String taskId);

    @Update("update task_queue set \"currentIndex\" = #{dbe.currentIndex}, key = #{dbe.key}, \"jsonString\" = #{dbe.jsonString}, " +
            "\"startTime\" = #{dbe.startTime}, \"updateTime\" = #{dbe.updateTime}, alias = #{dbe.alias} , \"life\" = #{dbe.life}, " +
            " \"futureStart\" = #{dbe.futureStart}, \"serverStopTime\" = #{dbe.serverStopTime}" +
            "where \"taskId\" = #{dbe.taskId}")
    boolean updateEntry(@Param("dbe")DatabaseEntry entry);

    @Delete("delete from task_queue where \"taskId\" = #{taskId}")
    boolean remove(String taskId);

    @Select("select * from task_queue where alias = #{alias}")
    List<DatabaseEntry> selectAllEntries(String alias);

    @Update("update task_queue set \"life\" = #{life}, \"futureStart\" = #{futureStart}, " +
            "\"serverStopTime\" = #{serverStopTime} where \"taskId\" = #{taskId} ")
    boolean whenClose(Long life, Long futureStart, Long serverStopTime, String taskId);

    @Insert("insert into task_instance_history values (#{h.task_id}, #{h.size}, #{h.index}, #{h.key}" +
            ", #{h.param}, #{h.start_time}, #{h.end_time}, #{h.alias}, #{h.result})")
    void writeHistory(@Param("h") InstanceHistory history);
}
