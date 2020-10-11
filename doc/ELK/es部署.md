[hadoop@hadoop01 ~]$ cd software/
[hadoop@hadoop01 software]$ tar -xvf elasticsearch-7.9.2-linux-x86_64.tar.gz -C ~/app/
[hadoop@hadoop01 software]$ cd ~/app/
[hadoop@hadoop01 app]$ ln -s elasticsearch-7.9.2 elasticsearch
[hadoop@hadoop01 app]$ echo -e '# ELASTICSEARCH ENV\nexport ELASTICSEARCH_HOME=/home/hadoop/app/elasticsearch\nexport PATH=$ELASTICSEARCH_HOME/bin:$PATH' >> ~/.bashrc
[hadoop@hadoop01 app]$ source ~/.bashrc 

[hadoop@hadoop01 app]$ vim ~/app/elasticsearch/config/jvm.options
-Xms256m
-Xmx256m

[hadoop@hadoop01 app]$ mkdir -p /home/hadoop/tmp/es/data
[hadoop@hadoop01 app]$ mkdir -p /home/hadoop/tmp/es/logs
[hadoop@hadoop01 app]$ vim ~/app/elasticsearch/config/elasticsearch.yml
cluster.name: elasticsearch
node.name: hadoop01
path.data: /home/hadoop/tmp/es/data
path.logs: /home/hadoop/tmp/es/logs
network.host: 0.0.0.0
http.port: 9200
cluster.initial_master_nodes: ["hadoop01"]
http.cors.enabled: true
http.cors.allow-origin: "*"

# 启动
[hadoop@hadoop01 ~]$ cd $ELASTICSEARCH_HOME
[hadoop@hadoop01 elasticsearch]$ ./bin/elasticsearch -d

# 启动报错
ERROR: [1] bootstrap checks failed
[1]: max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
ERROR: Elasticsearch did not exit normally - check the logs at /home/hadoop/tmp/es/logs/elasticsearch.log 


[hadoop@hadoop01 config]$ su - root
密码：
上一次登录：四 10月  1 00:01:54 CST 2020pts/2 上
[root@hadoop01 ~]# sysctl -w vm.max_map_count=655360
vm.max_map_count = 655360
[root@hadoop01 ~]# sysctl -a | grep "vm.max_map_count"
vm.max_map_count = 655360

