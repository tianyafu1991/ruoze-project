#!/bin/sh


## 本机虚拟机的提交脚本

spark-submit \
--name spark_hbase_etl \
--class com.ruoze.bigdata.tututuhomework.day20201019.demo.SparkHBaseApp \
--master yarn \
--deploy-mode client \
--driver-memory 1g \
--executor-memory 2g \
--executor-cores 2 \
--num-executors 2 \
--conf "spark.dw.raw.path=/ruozedata/dw/raw/hbase" \
--conf "spark.etl.hostname=hadoop01" \
--conf "spark.hbase.zookeeper.quorum=hadoop01:2181" \
--conf "spark.hbase.table=ruozedata:access_log" \
--conf "spark.stat.mysql.table=dwd_access_province_traffic" \
--conf "spark.execute.time=20190101" \
--jars /home/hadoop/app/ruoze-spark/lib/ip2region-1.7.2.jar,/home/hadoop/app/ruoze-spark/lib/ip2region.db,/home/hadoop/app/ruoze-spark/lib/mysql-connector-java-5.1.47.jar,/home/hadoop/app/hbase/lib/hbase-client-1.2.0-cdh5.16.2.jar,/home/hadoop/app/hbase/lib/hbase-server-1.2.0-cdh5.16.2.jar,/home/hadoop/app/hbase/lib/hbase-common-1.2.0-cdh5.16.2.jar,/home/hadoop/app/hbase/lib/hbase-protocol-1.2.0-cdh5.16.2.jar,/home/hadoop/app/hbase/lib/htrace-core-3.2.0-incubating.jar \
/home/hadoop/app/ruoze-spark/lib/ruoze-homework-1.0.jar