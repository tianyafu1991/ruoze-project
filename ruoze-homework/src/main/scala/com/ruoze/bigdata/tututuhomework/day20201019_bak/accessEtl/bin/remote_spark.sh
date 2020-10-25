#!/bin/sh

spark-submit --master yarn \
--deploy-mode client \
--class com.ruoze.bigdata.tututuhomework.day20201019.accessEtl.AccessETL2HBaseApp \
--driver-memory 1g \
--executor-memory 2g \
--executor-cores 2 \
--num-executors 1 \
--driver-java-options "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8787" \
--jars /home/hadoop/lib/ip2region-1.7.2.jar,/home/hadoop/lib/ip2region.db,/home/hadoop/lib/mysql-connector-java-5.1.47.jar \
/home/hadoop/app/ruoze-spark/lib/ruoze-homework-1.0.jar