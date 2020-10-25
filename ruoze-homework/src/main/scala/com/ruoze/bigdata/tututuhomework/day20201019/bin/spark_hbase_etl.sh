#!/bin/sh

spark-submit \
--name spark_hbase_etl \
--class com.ruoze.bigdata.tututuhomework.day20201019.SparkHBaseETLApp \
--master yarn \
--deploy-mode client \
--driver-memory 1g \
--executor-memory 2g \
--executor-cores 2 \
--num-executors 2 \
--conf "spark.dw.raw.path=/ruozedata/dw/raw/hbase" \
--conf "spark.etl.hostname=/ruozedata/dw/raw/hbase" \
--conf "spark.hbase.zookeeper.hostname=ruozedata001" \
--conf "spark.execute.time=20190101" \
--jars /root/app/ruozedata_spark/lib/ip2region-1.7.2.jar,/root/app/ruozedata_spark/lib/ip2region.db,/root/app/ruozedata_spark/lib/mysql-connector-java-5.1.47.jar,/root/app/hbase/lib/hbase-client-1.2.0-cdh5.16.2.jar,/root/app/hbase/lib/hbase-server-1.2.0-cdh5.16.2.jar,/root/app/hbase/lib/hbase-common-1.2.0-cdh5.16.2.jar,/root/app/hbase/lib/hbase-protocol-1.2.0-cdh5.16.2.jar,/root/app/hbase/lib/htrace-core-3.2.0-incubating.jar \
/root/app/ruozedata_spark/lib/ruoze-homework-1.0.jar