create table if not exists ${tableName}(
    id varchar(255) primary key,
    model varchar(255),
    level varchar(255),
    url varchar(255),
    request_method varchar(255),
    java_method varchar(255),
    controller_name varchar(50),
    oper_name varchar(50),
    oper_ip varchar(50),
    oper_param varchar(1000),
    json_result varchar(1000),
    status varchar(50),
    error_msg varchar(255),
    oper_time varchar(50),
    error_stack varchar(1000)
        );