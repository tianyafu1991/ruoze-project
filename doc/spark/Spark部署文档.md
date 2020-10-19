# 解压
```

[hadoop@ruozedata001 ~]$ cd ~/software
[hadoop@ruozedata001 software]$ tar -xvf ~/software/spark-2.4.6-bin-2.6.0-cdh5.16.2.tgz -C ~/app/
[hadoop@ruozedata001 software]$ cd ~/app/
[hadoop@ruozedata001 app]$ ln -s spark-2.4.6-bin-2.6.0-cdh5.16.2 spark

[hadoop@ruozedata001 app]$ cp spark/conf/spark-env.sh.template spark/conf/spark-env.sh
[hadoop@ruozedata001 app]$ cp spark/conf/spark-defaults.conf.template spark/conf/spark-defaults.conf
```