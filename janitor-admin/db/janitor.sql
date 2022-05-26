-- auto-generated definition
create table app_event
(
    id             bigint auto_increment primary key,
    app_name       varchar(100)              not null comment '应用名称',
    event_type     varchar(100)              not null comment '事件类型',
    event_value  varchar(2048) default '2' null comment '事件内容',
    create_time    datetime                  null comment '创建时间',
    update_time    datetime                  null comment '更新时间',
    target_ip_list varchar(1024)             null comment '推送ip列表'
)
    comment '应用事件';

create index idx_app_name
    on t_app_event (app_name)
    comment 'appName索引';

-- auto-generated definition
create table app_event_detail
(
    id            bigint auto_increment primary key,
    event_id      bigint               not null comment '事件id',
    app_name      varchar(100)         not null comment '应用名称',
    target_ip     varchar(200)         not null comment '推送ip',
    push_result   tinyint(1) default 2 null comment '推送结果 2-未知 1成功 1-失败',
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

