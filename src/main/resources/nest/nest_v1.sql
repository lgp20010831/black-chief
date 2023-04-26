create table if not exists t_nest(
    id varchar(255) PRIMARY KEY,
    parent_name varchar(255),
    son_name varchar(255),
    one_many int,
    parent_key varchar(255),
    son_key varchar(255),
    apply_sql varchar(255),
    suffix varchar(255),
    created_at varchar(255)
);


create table if not exists t_dict(
    id varchar(255) PRIMARY KEY,
    dict_table_name varchar(255),
    source_table_name varchar(255),
    p_code_name varchar(255),
    p_code_value varchar(255),
    code_name varchar(255),
    source_field_name varchar(255),
    result_name varchar(255),
    result_name_alias varchar(255),
    created_at varchar(255)
);
