#!/bin/bash

TIME=`date "+%Y%m%d%H%M%S"`

hdfs dfs -get /rzdolphinscheduler ./

tar -czf dolphinscheduler_hdfs_${TIME}.tar.gz rzdolphinscheduler/*

mv dolphinscheduler_hdfs_${TIME}.tar.gz /ruozedata/backup/

rm -rf rzdolphinscheduler
