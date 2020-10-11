1.在hive-site.xml中开启 hive.metastore.uris

<property>
<name>hive.metastore.uris</name>
<value>thrift://hadoop:9083</value>
</property>

并需要启动 nohup ~/app/hive/bin/hive --service metastore &