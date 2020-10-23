# HBase建表
create_namespace 'ruozedata'
create 'ruozedata:access_log' ,'f'

truncate 'ruozedata:access_log'


# MySQL建表
create table dwd_access_province_traffic(
id int primary key auto_increment comment '自增id',
province varchar(20) comment '省份',
traffics int comment '流量',
cnt int comment '次数',
create_time timestamp not null default current_timestamp comment '创建时间',
update_time timestamp not null default current_timestamp on update current_timestamp comment '更新时间'
)engine=innodb charset utf8 comment '省份流量统计表';