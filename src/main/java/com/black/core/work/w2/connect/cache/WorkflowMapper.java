package com.black.core.work.w2.connect.cache;


import com.black.core.work.w2.connect.entry.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@SuppressWarnings("all")
public interface WorkflowMapper {

//    查询
    @Select("select * from workflow")
    List<WorkflowEntry> queryWorkflows();

    @Select("select * from workflow_route")
    List<WorkflowRouteEntry> queryRoutes();

    @Select("select * from workflow_node_module")
    List<WorkflowNodeModuleEntry> queryNodeDefinitionals();

    @Select("select * from workflow_node_instance")
    List<WorkflowNodeInstanceEntry> queryNodeInstances();

    @Select("select * from workflow_instance")
    List<WorkflowInstanceEntry> queryInstances();

    @Select("select * from workflow_history_instance")
    List<WorkflowHistoryInstanceEntry> queryHistotyInstances();

    @Select("select * from workflow_node_instance_details")
    List<WorkflowNodeInstanceDetailsEntry> queryNodeInstanceDetails();

    @Select("select * from workflow_node_instance_details where \"instanceId\" = #{id}")
    List<WorkflowNodeInstanceDetailsEntry> queryNodeInstanceDetailsByInstanceId(String id);

    @Select("select * from workflow_instance where id = #{id}")
    WorkflowInstanceEntry queryInstanceById(String id);

    //添加
    @Insert("insert into workflow_node_module values (#{ne.id}, #{ne.name}, #{ne.attributeKeys})")
    void writeNodeModule(@Param("ne") WorkflowNodeModuleEntry nodeEntry);

    @Insert("insert into workflow_node_instance values (#{ie.id}, #{ie.name}, #{ie.moduleId}, #{ie.moduleName}, #{ie.workflowId}, " +
            "#{ie.workflowName}, #{ie.attributes}, #{ie.level}, #{ie.head}, #{ie.tail})")
    void writeNodeInstance(@Param("ie") WorkflowNodeInstanceEntry instanceEntry);

    @Insert("insert into workflow_route values (#{rou.id}, #{rou.name}, #{rou.workflowId}, #{rou.startAlias}, #{rou.endAlias}, " +
            "#{rou.startNodeId}, #{rou.endNodeId}, #{rou.conditional})")
    void writeRoute(@Param("rou") WorkflowRouteEntry routeEntry);

    @Insert("insert into workflow values (#{we.id}, #{we.name}, #{we.status}, #{we.type}, #{we.formDataAttribute})")
    void writeEntry(@Param("we") WorkflowEntry workflowEntry);

    @Insert("insert into workflow_instance values (#{ie.id}, #{ie.workflowId}, #{ie.routePath}, #{ie.currentNodeName}, #{ie.createTime}, #{ie.updateTime}, " +
            "#{ie.nodeScheduledTime}, #{ie.formData}, #{ie.properties})")
    void writeInstance(@Param("ie") WorkflowInstanceEntry instanceEntry);

    @Insert("insert into workflow_node_instance_details values (#{de.instanceId}, #{de.nodeInstanceId}, #{de.invokeTime}, " +
            "#{de.finishTime}, #{de.nodeName}, #{de.invoke}, #{de.hasBlocking}, #{de.result}, #{de.level})")
    void writeNodeInstanceDetails(@Param("de") WorkflowNodeInstanceDetailsEntry detailsEntry);

    @Insert("insert into workflow_history_instance values (#{e.instanceId}, #{e.workflowId}, #{e.routePath}, " +
            "#{e.createTime}, #{e.updateTime}, #{e.result}, #{e.formData}, #{e.properties})")
    void writeHistoryInstance(@Param("e") WorkflowHistoryInstanceEntry entry);


//    更新

    @Update("update workflow_instance set \"routePath\" = #{e.routePath}, \"currentNodeName\" = #{e.currentNodeName}, \"createTime\" = #{e.createTime}, " +
            "\"updateTime\" = #{e.updateTime}, \"nodeScheduledTime\" = #{e.nodeScheduledTime}, \"formData\" = #{e.formData}," +
            " properties = #{e.properties} where id = #{e.id}")
    void updateInstance(@Param("e") WorkflowInstanceEntry e);

    @Update("update workflow_node_module set name = #{nm.name}, \"attributeKeys\" = #{nm.attributeKeys} where id = #{nm.id}")
    void updateNodeModule(@Param("nm") WorkflowNodeModuleEntry nm);

    @Update("update workflow_node_instance_details set " +
            "  \"finishTime\" = #{de.finishTime}, \"nodeName\" = #{de.nodeName}, invoke = #{de.invoke}, \"hasBlocking\" = #{de.hasBlocking}," +
            " result = #{de.result}, level = #{de.level} where 'instanceId' = #{de.instanceId} and 'nodeInstanceId' = #{de.nodeInstanceId}")
    void updateNodeInstanceDetails(@Param("de") WorkflowNodeInstanceDetailsEntry detailsEntry);

    @Update("update workflow set status = #{status} where name = #{name}")
    void updateWorkflowStatus(String status, String name);

    @Delete("delete from workflow_node_module where id = #{id}")
    void removeNodeModule(String id);

    @Delete("delete from workflow_node where name = #{alias}")
    void removeNode(String alias);

    @Delete("delete from workflow where id = #{id}")
    void removeWorkflow(String id);

    @Delete("delete from workflow_node_instance where \"workflowId\"=#{id}")
    void removeNodeInstanceByWorkflowId(String id);

    @Delete("delete from workflow_node_instance where id =#{id}")
    void removeNodeInstanceById(String id);

    @Delete("delete from workflow_route where id =#{id}")
    void removeNodeRouteById(String id);

    @Delete("delete from workflow_route where \"workflowId\"=#{id}")
    void removeNodeRouteByWorkflowId(String id);

    @Delete("delete from workflow_instance where id = #{id}")
    void removeInstance(String id);

    @Delete("delete from workflow_node_instance_details where \"instanceId\" = #{id}")
    void removeNodeInstanceDetails(String id);
}
