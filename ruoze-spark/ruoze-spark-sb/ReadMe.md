请尝试使用sb整合spark开发一个长服务作业，完成如下要求：
* 对接access日志，发起一个web请求：
http://ruozedata001:9527/spark/access/20201104，就可以完成20201104对应日志的etl作业


```$xslt

[hadoop@hadoop ~]$ mkdir ~/app/ruozedata-spark-sb
[hadoop@hadoop ~]$ cd ~/app/ruozedata-spark-sb/
[hadoop@hadoop ruozedata-spark-sb]$ mkdir lib bin data
```


#坑
```$xslt
要将springboot打包的插件去掉，要不然打出来的jar包里面的目录结构与spark正常打包的结构不一致，导致提交任务的时候找不到主类



jar包冲突：
The method's class, javax.validation.BootstrapConfiguration, is available from the following locations:

    jar:file:/home/hadoop/app/spark-2.4.6-bin-2.6.0-cdh5.16.2/jars/validation-api-1.1.0.Final.jar!/javax/validation/BootstrapConfiguration.class
    jar:file:/home/hadoop/app/ruozedata-spark-sb/lib/spring_lib/jakarta.validation-api-2.0.2.jar!/javax/validation/BootstrapConfiguration.class


[hadoop@hadoop jars]$ mkdir -p ~/tmp/spark/jars/bak/
[hadoop@hadoop jars]$ mv ~/app/spark/jars/validation-api-1.1.0.Final.jar ~/tmp/spark/jars/bak/
[hadoop@hadoop jars]$ 

(2)
The method's class, com.google.gson.GsonBuilder, is available from the following locations:

    jar:file:/home/hadoop/app/spark-2.4.6-bin-2.6.0-cdh5.16.2/jars/gson-2.2.4.jar!/com/google/gson/GsonBuilder.class

It was loaded from the following location:

    file:/home/hadoop/app/spark-2.4.6-bin-2.6.0-cdh5.16.2/jars/gson-2.2.4.jar

[hadoop@hadoop jars]$ mv ~/app/spark/jars/gson-2.2.4.jar ~/tmp/spark/jars/bak/


```