CREATE TABLE spark_log_etl_conf (
	id INT PRIMARY KEY auto_increment COMMENT '主键自增',
	conf_name VARCHAR (255) COMMENT '配置名称',
	domain VARCHAR (255) COMMENT '域名',
	compress_codec VARCHAR (255) COMMENT '压缩格式',
	file_name_prefix VARCHAR (255) COMMENT '文件名前缀',
	file_name_time_format VARCHAR (255) COMMENT '文件名的时间格式',
	file_name_suffix VARCHAR (255) COMMENT '文件名后缀',
	log_content_fields VARCHAR (255) COMMENT '日志文件所需字段',
	fields_delimiter VARCHAR (255) COMMENT '字段内容分隔符',
	create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 comment 'spark日志解析配置表';