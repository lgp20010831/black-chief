create table if not exists workflow(
    id varchar(255) PRIMARY KEY,
    name varchar(255),
    status varchar(255),
    type varchar(255),
    "formDataAttribute" varchar(255)
);


create table if not exists workflow_node_module(
    id varchar(255) PRIMARY KEY,
    name varchar(255),
    "attributeKeys" varchar(255)
);


create table if not exists workflow_node_instance(
    id varchar(255) PRIMARY KEY,
--     节点实例在工作流中的名字
    name varchar(255),
    "moduleId" varchar(255),
    "moduleName" varchar(255),
    "workflowId" varchar(255),
    "workflowName" varchar(255),
--     属性, 节点自身的属性
    attributes varchar(1000),
--     层级, 该实例在工作流模板中的层级
    level int,
    head boolean,
    tail boolean
);

create table if not exists workflow_node_instance_details(
    "instanceId" varchar(255),
    "nodeInstanceId" varchar(255),
    "invokeTime" varchar(255),
    "finishTime" varchar(255),
    "nodeName" varchar(255),
    invoke boolean,
    "hasBlocking" boolean,
    result boolean,
    level int
);

create table if not exists workflow_route(
    id varchar(255) PRIMARY KEY,
    name varchar(255),
    "workflowId" varchar(255),
--     起始节点实例名字
    "startAlias" varchar(255),
--     结束节点实例名字
    "endAlias" varchar(255),
--     起始节点实例 id
    "startNodeId" varchar(255),
--     结束节点实例 id
    "endNodeId" varchar(255),
    conditional varchar(500)
);

-- 实例的历史表
create table if not exists workflow_history_instance(
    "instanceId" varchar(255) PRIMARY KEY,
    "workflowId" varchar(255),
    "routePath" varchar(1000),
    "createTime" varchar(255),
    "updateTime" varchar(255),
    result varchar(255),
    --     表单数据
    "formData" varchar(1000),
--     全局属性
    properties varchar(1000)
);

-- 正在进行的实例表
create table if not exists workflow_instance(
    id varchar(255) PRIMARY KEY,
    "workflowId" varchar(255),
    "routePath" varchar(1000),
--     当前正在进行的节点, 一定是一个 array, 可能同时有多个节点正在进行, 比如会签
    "currentNodeName" varchar(1000),
    "createTime" varchar(255),
    "updateTime" varchar(255),
--     当前正在进行的节点是否有时间限制
    "nodeScheduledTime" varchar(500),
--     表单数据
    "formData" varchar(1000),
--     全局属性
    properties varchar(1000)
);