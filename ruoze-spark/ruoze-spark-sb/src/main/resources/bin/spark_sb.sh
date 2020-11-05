#!/bin/sh

spark-submit \
--name spark_sb_etl \
--class com.ruozedata.ruozesparksb.RuozeSparkSbApplication \
--master yarn \
--deploy-mode client \
--driver-memory 1g \
--executor-memory 2g \
--executor-cores 2 \
--num-executors 2 \
--conf "spark.dw.raw.path=/ruozedata/dw/raw/hbase" \
--conf "spark.hbase.zookeeper.quorum=hadoop:2181" \
--conf "spark.hbase.table=ruozedata:access_log" \
--conf "spark.etl.hostname=hadoop" \
--conf "spark.stat.mysql.table=dwd_access_province_traffic" \
--conf "spark.execute.time=20190101" \
--jars /home/hadoop/app/ruozedata-spark-sb/lib/third_lib/ip2region.db,/home/hadoop/app/ruozedata-spark-sb/lib/third_lib/*.jar,/home/hadoop/app/ruozedata-spark-sb/lib/spring_lib/*.jar,/home/hadoop/app/ruozedata-spark-sb/lib/hbase_lib/*.jar \
/home/hadoop/app/ruozedata-spark-sb/lib/ruoze-spark-sb-1.0.jar