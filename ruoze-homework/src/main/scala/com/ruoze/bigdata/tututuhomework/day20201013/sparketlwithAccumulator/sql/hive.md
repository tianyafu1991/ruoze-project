create external table default.ods_access(
ip string ,
proxyIp string ,
responseTime bigint,
referer string ,
method string ,
url string ,
httpCode string ,
requestSize bigint,
responseSize bigint,
cache string ,
uaHead string ,
type string ,
province string ,
city string ,
isp string ,
http string ,
domain string ,
path string ,
params string ,
year string ,
month string ,
day string
) comment '日志清洗表' partitioned by (d string comment '分区字段') row format delimited fields terminated by '\t' location '/tmp/ruozedata/dw/ods/access';



create external table default.dwd_access_province_traffic(
province string,
traffics int,
cnt int
) comment '省份流量表' partitioned by (d string comment '日期分区(yyyyMMdd)')  row format delimited fields terminated by '\t' location '/tmp/ruozedata/dw/dwd/access_province_traffic' ;