#!/bin/sh

spark-submit \
--name spark_hbase_etl \
--class com.ruoze.bigdata.tututuhomework.day20201019.accessEtl.AccessETL2HBaseApp \
--master yarn \
--deploy-mode client \
--driver-memory 1g \
--executor-memory 2g \
--executor-cores 2 \
--num-executors 2 \
--conf "spark.dw.raw.path=/ruozedata/dw/raw/access" \
--conf "spark.execute.time=20190101" \
--jars /home/hadoop/lib/ip2region-1.7.2.jar,/home/hadoop/lib/ip2region.db,/home/hadoop/app/hbase/lib/hbase-*.jar,/home/hadoop/app/hbase/lib/htrace-core4-4.0.1-incubating.jar \
--driver-class-path /home/hadoop/lib/ip2region-1.7.2.jar,/home/hadoop/lib/ip2region.db,/home/hadoop/app/hbase/lib/hbase-*.jar,/home/hadoop/app/hbase/lib/htrace-core4-4.0.1-incubating.jar \
/home/hadoop/app/ruoze-spark/lib/ruoze-homework-1.0.jar