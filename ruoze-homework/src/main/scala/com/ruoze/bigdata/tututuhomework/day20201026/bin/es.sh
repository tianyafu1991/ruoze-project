#!/bin/sh


## 本机虚拟机的提交脚本

spark-submit \
--name spark_es_etl \
--class com.ruoze.bigdata.tututuhomework.day20201026.app.SparkESApp \
--master yarn \
--deploy-mode client \
--driver-memory 1g \
--executor-memory 2g \
--executor-cores 2 \
--num-executors 2 \
--conf "spark.dw.raw.path=/ruozedata/dw/raw/hbase" \
--conf "spark.use.df=false" \
--conf "spark.es.nodes=hadoop" \
--conf "spark.es.port=9200" \
--conf "spark.es.resource=access_log_rdd/" \
--conf "spark.execute.time=20190101" \
--jars /home/hadoop/lib/ip2region-1.7.2.jar,/home/hadoop/lib/ip2region.db,/home/hadoop/lib/mysql-connector-java-5.1.47.jar \
/home/hadoop/app/ruoze-spark/lib/ruoze-homework-1.0.jar