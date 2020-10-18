#!/bin/sh

spark-submit \
--name ProvinceSQL2RDDApp \
--class com.tianya.bigdata.homework.day20200901.sparketl.etl.ProvinceSQL2RDDApp \
--master yarn \
--deploy-mode client \
--driver-memory 1g \
--executor-memory 2g \
--executor-cores 2 \
--num-executors 2 \
/home/hadoop/app/ruozedata-spark/lib/ruozedata-homework-1.0.jar \
20190101