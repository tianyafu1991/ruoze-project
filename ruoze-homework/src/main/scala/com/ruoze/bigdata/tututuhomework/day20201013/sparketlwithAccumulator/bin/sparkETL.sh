#!/bin/sh

spark-submit \
--name spark_etl \
--class com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.etl.SparkETL2 \
--master yarn \
--deploy-mode client \
--driver-memory 1g \
--executor-memory 1g \
--executor-cores 1 \
--num-executors 1 \
--conf "spark.dw.raw.path=/ruozedata/dw/raw/access" \
--conf "spark.dw.tmp.path=/ruozedata/dw/ods_tmp/access" \
--conf "spark.dw.ods.path=/ruozedata/dw/ods/access" \
--conf "spark.execute.time=20190101" \
--jars /root/lib/ip2region-1.7.2.jar,/root/lib/ip2region.db \
/root/app/ruozedata_dw/lib/ruoze-homework-1.0.jar