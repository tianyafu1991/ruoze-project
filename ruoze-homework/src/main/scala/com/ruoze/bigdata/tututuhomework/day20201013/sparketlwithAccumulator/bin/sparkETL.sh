#!/bin/sh

spark-submit \
--name spark_etl \
--class com.tianya.bigdata.homework.day20200901.sparketl.etl.SparkETL3 \
--master yarn \
--deploy-mode client \
--driver-memory 1g \
--executor-memory 2g \
--executor-cores 2 \
--num-executors 2 \
--jars hdfs://hadoop01:9000/ruozedata/dw/data/ip2region.db,hdfs://hadoop01:9000/ruozedata/dw/lib/ip2region-1.7.2.jar \
/home/hadoop/app/ruozedata-spark/lib/ruozedata-homework-1.0.jar