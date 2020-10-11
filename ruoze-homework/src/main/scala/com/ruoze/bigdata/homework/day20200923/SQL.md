create table offsets_storage(
topic varchar(50),
groupid varchar(50),
`partitions` int,
`offset` bigint
)engine=innodb charset utf8mb4;