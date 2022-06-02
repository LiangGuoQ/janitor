-- auto-generated definition
create table t_app_event
(
    id             bigint auto_increment primary key,
    app_name       varchar(100)              not null comment '应用名称',
    event_type     varchar(100)              not null comment '事件类型',
    event_value    varchar(2048) default '2' null comment '事件内容',
    create_time    datetime                  null comment '创建时间',
    update_time    datetime                  null comment '更新时间',
    target_ip_list varchar(1024)             null comment '推送ip列表'
)
    comment '应用事件';

create index idx_app_name
    on t_app_event (app_name)
    comment 'appName索引';

-- auto-generated definition
create table t_app_event_detail
(
    id            bigint auto_increment primary key,
    event_id      bigint               not null comment '事件id',
    app_name      varchar(100)         not null comment '应用名称',
    target_ip     varchar(200)         not null comment '推送ip',
    push_result   tinyint(1) default 2 null comment '推送结果 2-未知 1-成功 0-失败',
    error_content varchar(1024)        null comment '错误信息',
    create_time   datetime             null comment '创建时间',
    update_time   datetime             null comment '更新时间',
    retry_active  int        default 0 null comment '实际重试次数',
    retry_plan    int        default 0 null comment '计划重试次数'
)
    comment '应用事件明细';

create index idx_push_result_index
    on t_app_event_detail (push_result);

create index idx_event_id
    on t_app_event_detail (event_id)
    comment '关联事件id索引';

-- auto-generated definition
create table t_app_conf
(
    id          bigint auto_increment comment '主键ID' primary key,
    app_name    varchar(100)         not null comment '应用名称',
    conf_key    varchar(128)         not null comment '应用配置KEY',
    conf_value  varchar(1024)        null comment '应用配置内容',
    remark      varchar(256)         null comment '备注',
    status      tinyint(1) default 1 null comment '0-已发布 1-新增 2-修改 3-删除 4-发布后修改',
    create_time datetime             null comment '创建时间',
    update_time datetime             null comment '更新时间',
    constraint uk_app_name_conf_key
        unique (app_name, conf_key)
)
    comment '应用配置';

-- auto-generated definition
create table t_app_conf_his
(
    id          bigint auto_increment comment '主键ID' primary key,
    version     bigint        not null comment '应用配置版本',
    app_name    varchar(100)  not null comment '应用名称',
    conf_key    varchar(128)  not null comment '应用配置KEY',
    conf_value  varchar(1024) null comment '应用配置内容',
    remark      varchar(256)  null comment '备注',
    create_time datetime      null comment '创建时间',
    update_time datetime      null comment '更新时间'
)
    comment '应用配置历史记录表';

-- auto-generated definition
create table t_app_version
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    app_name    varchar(100) not null comment '应用名称',
    version     bigint       null comment '版本号',
    create_time datetime     null comment '创建时间',
    update_time datetime     null comment '更新时间',
    constraint uk_app_name
        unique (app_name)
)
    comment '应用版本表';

