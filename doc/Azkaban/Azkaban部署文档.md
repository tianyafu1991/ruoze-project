# 解压
[hadoop@ruozedata001 app]$ cd ~/software/
[hadoop@ruozedata001 software]$ tar -xvf azkaban-web-server-0.1.0-SNAPSHOT.tar.gz -C ~/app/
[hadoop@ruozedata001 software]$ tar xvf azkaban-exec-server-0.1.0-SNAPSHOT.tar.gz -C ~/app/
[hadoop@ruozedata001 software]$ tar -xvf azkaban-solo-server-0.1.0-SNAPSHOT.tar.gz -C ~/app/
[hadoop@ruozedata001 software]$ tar -xvf azkaban-db-0.1.0-SNAPSHOT.tar.gz -C ~/app/
# 创建软连接
[hadoop@ruozedata001 software]$ cd ~/app/
[hadoop@ruozedata001 app]$ ln -s azkaban-exec-server-0.1.0-SNAPSHOT azkaban-exec-server
[hadoop@ruozedata001 app]$ ln -s azkaban-web-server-0.1.0-SNAPSHOT azkaban-web-server
[hadoop@ruozedata001 app]$ ln -s azkaban-solo-server-0.1.0-SNAPSHOT azkaban-solo-server
[hadoop@ruozedata001 app]$ ln -s azkaban-db-0.1.0-SNAPSHOT azkaban-db

# 拷贝azkaban的元数据表建表语句到/tmp目录下
[hadoop@ruozedata001 app]$ cp ~/app/azkaban-db/create-all-sql-0.1.0-SNAPSHOT.sql /tmp

# 创建Azkaban的元数据库
[hadoop@ruozedata001 app]$ su - root
Password: 
Last login: Mon Oct 19 10:42:18 CST 2020 from 112.17.166.10 on pts/2
[root@ruozedata001 ~]# su - mysqladmin
Last login: Mon Oct 19 12:12:47 CST 2020 on pts/0
ruozedata001:mysqladmin:/usr/local/mysql:>mysql -uroot -p
Enter password: 
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 2
Server version: 5.7.26-log MySQL Community Server (GPL)

Copyright (c) 2000, 2019, Oracle and/or its affiliates. All rights reserved.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql> create database azkaban default character set utf8;
Query OK, 1 row affected (0.00 sec)

mysql> grant all privileges on root.* to root@'%' identified by 'tianyafu@123';
Query OK, 0 rows affected, 1 warning (0.00 sec)

mysql> flush PRIVILEGES;
Query OK, 0 rows affected (0.01 sec)

mysql> use azkaban
Database changed

mysql> source /tmp/create-all-sql-0.1.0-SNAPSHOT.sql

mysql> show tables;
+-----------------------------+
| Tables_in_azkaban           |
+-----------------------------+
| active_executing_flows      |
| active_sla                  |
| execution_dependencies      |
| execution_flows             |
| execution_jobs              |
| execution_logs              |
| executor_events             |
| executors                   |
| project_events              |
| project_files               |
| project_flow_files          |
| project_flows               |
| project_permissions         |
| project_properties          |
| project_versions            |
| projects                    |
| properties                  |
| qrtz_blob_triggers          |
| qrtz_calendars              |
| qrtz_cron_triggers          |
| qrtz_fired_triggers         |
| qrtz_job_details            |
| qrtz_locks                  |
| qrtz_paused_trigger_grps    |
| qrtz_scheduler_state        |
| qrtz_simple_triggers        |
| qrtz_simprop_triggers       |
| qrtz_triggers               |
| ramp                        |
| ramp_dependency             |
| ramp_exceptional_flow_items |
| ramp_exceptional_job_items  |
| ramp_items                  |
| triggers                    |
| validated_dependencies      |
+-----------------------------+
35 rows in set (0.00 sec)

mysql> quit;
Bye

# 配置环境变量
```

[hadoop@ruozedata001 ~]$ vim ~/.bashrc
# AZKABAN_ENV
export AZKABAN_EXECUTOR_HOME=/home/hadoop/app/azkaban-exec-server
export AZKABAN_WEB_HOME=/home/hadoop/app/azkaban-web-server
export PATH=${AZKABAN_EXECUTOR_HOME}/bin:${AZKABAN_WEB_HOME}/bin:$PATH
[hadoop@ruozedata001 ~]$ source ~/.bashrc

```

# 部署 Azkaban Executor
```
[hadoop@ruozedata001 ~]$ cd $AZKABAN_EXECUTOR_HOME/
[hadoop@ruozedata001 azkaban-exec-server]$ mkdir logs extlib
[hadoop@ruozedata001 azkaban-exec-server]$ vim conf/azkaban.properties
azkaban.name=tianyafu_azkaban
azkaban.label=this is my azkaban
default.timezone.id=Asia/Shanghai
azkaban.webserver.url=http://ruozedata001:8081
database.type=mysql
mysql.port=3306
mysql.host=ruozedata001
mysql.database=azkaban
mysql.user=root
mysql.password=tianyafu@123
executor.port=12321
azkaban.use.multiple.executors=true

[hadoop@ruozedata001 azkaban-exec-server]$ vim plugins/jobtypes/commonprivate.properties
# add by tianyafu
azkaban.native.lib=false

```

# 部署 Azkaban Web
```
[hadoop@ruozedata001 azkaban-exec-server]$ cd $AZKABAN_WEB_HOME/
[hadoop@ruozedata001 azkaban-web-server]$ mkdir logs extlib plugins temp
[hadoop@ruozedata001 azkaban-web-server]$ vim conf/azkaban.properties 
azkaban.name=tianyafu_azkaban
azkaban.label=this is my azkaban
default.timezone.id=Asia/Shanghai
database.type=mysql
mysql.port=3306
mysql.host=ruozedata001
mysql.database=azkaban
mysql.user=root
mysql.password=tianyafu@123
```

# 其他配置

## 修改启动脚本
```
[hadoop@ruozedata001 azkaban-web-server]$ cd $AZKABAN_EXECUTOR_HOME/
[hadoop@ruozedata001 azkaban-exec-server]$ cat bin/start-exec.sh 
#!/bin/bash

script_dir=$(dirname $0)

# pass along command line arguments to the internal launch script.
${script_dir}/internal/internal-start-executor.sh "$@" >executorServerLog__`date +%F+%T`.out 2>&1 &

## 从该启动脚本可以看出，在哪个目录启动，就在哪个目录产生日志文件，这样如果没有在固定目录启动azkaban，会导致日志到处都是
[hadoop@ruozedata001 azkaban-exec-server]$ vim bin/start-exec.sh 
#!/bin/bash

script_dir=$(dirname $0)

# pass along command line arguments to the internal launch script.
#${script_dir}/internal/internal-start-executor.sh "$@" >executorServerLog__`date +%F+%T`.out 2>&1 &
${script_dir}/internal/internal-start-executor.sh "$@" >${script_dir}/../logs/executorServerLog__`date +%F+%T`.out 2>&1 &


同理：
[hadoop@ruozedata002 azkaban-exec-server]$ cd $AZKABAN_WEB_HOME
[hadoop@ruozedata002 azkaban-web-server]$ vim bin/start-web.sh
#!/bin/bash

script_dir=$(dirname $0)

${script_dir}/internal/internal-start-web.sh >${script_dir}/../logs/webServerLog_`date +%F+%T`.out 2>&1 &
```
# 拷贝到其他节点
```
[hadoop@ruozedata001 ~]$ cd ~/app/
[hadoop@ruozedata001 app]$ scp -r azkaban-exec-server-0.1.0-SNAPSHOT hadoop@ruozedata002:/home/hadoop/app/
[hadoop@ruozedata001 app]$ scp -r azkaban-exec-server-0.1.0-SNAPSHOT hadoop@ruozedata003:/home/hadoop/app/

[hadoop@ruozedata001 app]$ scp -r azkaban-web-server-0.1.0-SNAPSHOT hadoop@ruozedata002:/home/hadoop/app/
[hadoop@ruozedata002 app]$ ln -s azkaban-web-server-0.1.0-SNAPSHOT azkaban-web-server


[hadoop@ruozedata001 app]$ rm -rf azkaban-web-server
[hadoop@ruozedata001 app]$ rm -rf azkaban-web-server-0.1.0-SNAPSHOT
```

# 启动
```
[hadoop@ruozedata002 app]$ cd $AZKABAN_EXECUTOR_HOME
[hadoop@ruozedata002 azkaban-exec-server]$ ./bin/start-exec.sh
curl -G "ruozedata001:12321/executor?action=activate" && echo
curl -G "ruozedata002:12321/executor?action=activate" && echo
curl -G "ruozedata003:12321/executor?action=activate" && echo

[hadoop@ruozedata002 azkaban-exec-server]$ cd $AZKABAN_WEB_HOME
[hadoop@ruozedata002 azkaban-web-server]$ ./bin/start-web.sh


```

# 第三方插件部署
```
[hadoop@ruozedata001 ~]$ cd app/azkaban-exec-server
[hadoop@ruozedata001 azkaban-exec-server]$ cd plugins/jobtypes/
[hadoop@ruozedata001 jobtypes]$ ll
total 67596
-rw-r--r-- 1 hadoop hadoop 69210207 Aug 22 07:14 azkaban-jobtype-3.0.0.tar.gz
-rw-r--r-- 1 hadoop hadoop       88 Oct 19 13:08 commonprivate.properties
[hadoop@ruozedata001 jobtypes]$ mv commonprivate.properties commonprivate.properties.bak
[hadoop@ruozedata001 jobtypes]$ tar -xvf azkaban-jobtype-3.0.0.tar.gz 
[hadoop@ruozedata001 jobtypes]$ mv azkaban-jobtype-3.0.0/* .

[hadoop@ruozedata001 jobtypes]$ vim commonprivate.properties
# add by tianyafu @20201019
execute.as.user=false
azkaban.should.proxy=false
hadoop.home=/opt/cloudera/parcels/CDH/lib/hadoop
obtain.binary.token=false
hive.home=/opt/cloudera/parcels/CDH/lib/hive
spark.home=/home/hadoop/app/spark
azkaban.native.lib=false
hadoop.classpath=${hadoop.home}/etc/hadoop,/home/hadoop/app/azkaban-exec-server/extlib/*
jobtype.global.classpath=${hadoop.home}/etc/hadoop,/home/hadoop/app/azkaban-exec-server/extlib/*

# 配置Hive
[hadoop@ruozedata001 jobtypes]$ vim hive/plugin.properties 
# add by tianyafu @20201018
hive.home=/opt/cloudera/parcels/CDH/lib/hive
hive.aux.jars.path=${hive.home}/auxlib
hive.jvm.args=-Dhive.querylog.location=. -Dhive.exec.scratchdir=/tmp/hive-${user.to.proxy} -Dhive.aux.jars.path=${hive.aux.jars.path}
jobtype.jvm.args=${hive.jvm.args}

[hadoop@ruozedata001 jobtypes]$ vim hive/private.properties
jobtype.classpath=${hadoop.home}/etc/hadoop,/home/hadoop/app/azkaban-exec-server/extlib/*,${hive.home}/lib/*,${hive.home}/conf,${hive.aux.jar.path}/*

[hadoop@ruozedata001 jobtypes]$ vim $AZKABAN_EXECUTOR_HOME/bin/internal/internal-start-executor.sh
CLASSPATH=$CLASSPATH:$azkaban_dir/extlib/*

[hadoop@ruozedata001 jobtypes]$ cd $AZKABAN_EXECUTOR_HOME/plugins/


```