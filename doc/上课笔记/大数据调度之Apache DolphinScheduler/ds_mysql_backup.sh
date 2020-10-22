#!/bin/bash

/usr/local/mysql/bin/mysqldump \
-uroot -pRz123456! -hruozedata001 \
--single-transaction \
--socket=/usr/local/mysql/data/mysql.sock \
--master-data=2 --databases rzdolphinscheduler > /ruozedata/backup/dolphinscheduler_mysql_`date "+%Y%m%d%H%M%S"`.sql

