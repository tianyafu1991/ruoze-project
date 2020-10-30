#!/bin/sh

source /home/hadoop/app/ruoze-recommend/bin/env.sh

## source的配置
SORUCE_FORMAT="jdbc"
SORUCE_TABLE="member"
SOURCE_VIEW_NAME="member"

## 统计的sql
STAT_SQL="select channel,count(1) cnt from member group by channel"

## Hive的配置
HIVE_FILE_FORMAT="orc"
HIVE_COMPRESSION="zlib"
HIVE_DATABASE="dw"
HIVE_TABLE_NAME="app_channel_stat"
HIVE_SAVE_MODE="overwrite"

## target的配置
TARGET_FORMAT="jdbc"
TARGET_TABLE="channel_stat"
TARGET_SAVE_MODE="overwrite"

spark-submit \
--name order_stat \
--class com.ruoze.bigdata.homework.day20201017.CommonApp \
--master yarn \
--deploy-mode client \
--driver-memory 1g \
--executor-memory 2g \
--executor-cores 2 \
--num-executors 2 \
--conf "spark.source.format=${SORUCE_FORMAT}" \
--conf "spark.source.url=${SORUCE_MYSQL_URL}" \
--conf "spark.source.table=${SORUCE_TABLE}" \
--conf "spark.source.user=${SOURCE_MYSQL_USER}" \
--conf "spark.source.password=${SOURCE_MYSQL_PASSWORD}" \
--conf "spark.source.driver=${SOURCE_MYSQL_DRIVER}" \
--conf "spark.source.view.name=${SOURCE_VIEW_NAME}" \
--conf "spark.stat.sql=${STAT_SQL}" \
--conf "spark.hive.file.format=${HIVE_FILE_FORMAT}" \
--conf "spark.hive.compression=${HIVE_COMPRESSION}" \
--conf "spark.hive.database.name=${HIVE_DATABASE}" \
--conf "spark.hive.table.name=${HIVE_TABLE_NAME}" \
--conf "spark.hive.save.mode=${HIVE_SAVE_MODE}" \
--conf "spark.target.format=${TARGET_FORMAT}" \
--conf "spark.target.url=${TARGET_MYSQL_URL}" \
--conf "spark.target.table=${HIVE_SAVE_MODE}" \
--conf "spark.target.user=${TARGET_MYSQL_USER}" \
--conf "spark.target.password=${TARGET_MYSQL_PASSWORD}" \
--conf "spark.target.driver=${TARGET_MYSQL_DRIVER}" \
--conf "spark.target.save.mode=${HIVE_SAVE_MODE}" \
--jars /home/hadoop/lib/mysql-connector-java-5.1.47.jar \
/home/hadoop/app/ruoze-recommend/lib/ruoze-homework-1.0.jar