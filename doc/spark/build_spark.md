# 编译Spark2.4.6并导入IDEA，在IDEA中运行Spark SQL 对接Hive
```
编译版本信息
spark.version=2.4.6
scala.version=2.12.10
maven.version=3.6.3
java.version=1.8.0_181
windows上git版本 git.version=2.17.0
hadoop.version=2.6.0-cdh5.16.2
hive.version=1.1.0-cdh5.16.2

```

##下载源码包
```
https://archive.apache.org/dist/spark/spark-2.4.6/spark-2.4.6.tgz
```

## 在 git bash中编译
```
1.修改$SPARK_HOME/pom.xml文件：在repositories标签中新增阿里云的maven仓库地址和cloudera的仓库地址
<repository>
  <id>aliyun</id>
  <name>cloudera Repository</name>
  <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
</repository>

<repository>
  <id>cloudera</id>
  <name>cloudera Repository</name>
  <url>https://repository.cloudera.com/artifactory/cloudera-repos/</url>
</repository>
2.修改$SPARK_HOME/dev/make-distribution.sh脚本
(1)显式指定一些版本信息，跳过编译时的一些比较耗时的版本检查，提高编译效率
显式指定一下版本信息
VERSION=2.4.6
SCALA_VERSION=2.12
SPARK_HADOOP_VERSION=2.6.0-cdh5.16.2
SPARK_HIVE=1

并将脚本中原来的检查给注释掉(就是下面这段)
#VERSION=$("$MVN" help:evaluate -Dexpression=project.version $@ 2>/dev/null\
#    | grep -v "INFO"\
#    | grep -v "WARNING"\
#    | tail -n 1)
#SCALA_VERSION=$("$MVN" help:evaluate -Dexpression=scala.binary.version $@ 2>/dev/null\
#    | grep -v "INFO"\
#    | grep -v "WARNING"\
#    | tail -n 1)
#SPARK_HADOOP_VERSION=$("$MVN" help:evaluate -Dexpression=hadoop.version $@ 2>/dev/null\
#    | grep -v "INFO"\
#    | grep -v "WARNING"\
#    | tail -n 1)
#SPARK_HIVE=$("$MVN" help:evaluate -Dexpression=project.activeProfiles -pl sql/hive $@ 2>/dev/null\
#    | grep -v "INFO"\
#    | grep -v "WARNING"\
#    | fgrep --count "<id>hive</id>";\
#    # Reset exit status to 0, otherwise the script stops here if the last grep finds nothing\
#    # because we use "set -o pipefail"
#    echo -n)

(2)增加编译时的内存设置，防止OOM
修改这一行的内存配置，我这里统一放大了一倍
export MAVEN_OPTS="${MAVEN_OPTS:--Xmx4g -XX:ReservedCodeCacheSize=2g}"

3.修改scala的版本
./dev/change-scala-version.sh 2.12

4.编译期间需要下载(这个是在$SPARK_HOME/build/mvn的脚本中指定的)
这个zinc的版本和scala的版本要看一下mvn这个脚本确认一下
https://downloads.lightbend.com/zinc/0.3.15/zinc-0.3.15.tgz
https://downloads.lightbend.com/scala/2.11.12/scala-2.11.12.tgz
有时候下载比较慢，提前下载好之后放在$SPARK_HOME/build下面就行

5.注意：如果我们需要用编译好的spark包拿去部署，则这一步不建议在这里做，毕竟依赖一个hive-site.xml进去不好。
用IDEA导入源码，找到$SPARK_HOME/sql/hive-thriftserver这个Module，在里面新建一个resources目录，并右键标记位resources，将$Hive_HOME/conf下面的hive-site.xml拷贝到resources
这个步骤是在IDEA中完成的，如果跳过此步，不影响编译，但是运行org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver时找的是内置的Hive，源数据库是Derby
而且如果编译完再在IDEA中添加hive-site.xml，即使rebuild，这个文件也没有被编译到target目录，即不生效。到时候还是需要重新编译

6.编译：
./dev/make-distribution.sh --name 2.6.0-cdh5.16.2 --tgz -Phadoop-2.6 -Dhadoop.version=2.6.0-cdh5.16.2 -Dscala.version=2.12.10 -Phive -Phive-thriftserver -Pyarn

```

## git bash编译之后，需要在idea中再 Rebuild Project一下
```
1.勾选必要的 maven profiles
idea右边侧边栏maven，点开第一项Profiles
勾选hadoop-2.6、hive、hive-provided、hive-thriftserver、scala-2.12、yarn这几个profile

2.idea中安装antlr v4插件

3.在spark的core Module中，在resources目录下手动建一个spark-version-info.properties文件(在Windows的IDEA中编译源码，才会导致这个问题;不在Windows环境的IDEA中编译，这一步可跳过)
文件内容：
version=2.4.6
branch=
revision=
user=
url=
date=

这个需要看$SPARK_HOME/build/spark-build-info这个脚本，在这个脚本中会去创建spark-version-info.properties文件并echo一些信息进去
但这个是需要bash解释器和运行环境的，如果是在IDEA中去编译源码，因为没有bash运行环境，不会去运行这个文件，
会导致编译好的jar包中缺少spark-version-info.properties文件
在运行程序的时候报错：org.apache.spark.SparkException: Could not find spark-version-info.properties

4.找到$SPARK_HOME/sql/hive-thriftserver这个Module，在里面新建一个resources目录，并右键标记位resources，将$Hive_HOME/conf下面的hive-site.xml拷贝到resources
(这一步最好提前在git bash编译时做掉，这里就可以跳过这一步了)
如果没有在git bash的时候把hive-site.xml编译进去，那么这里将hive-site.xml拷贝进去之后要重新编译，否则target目录中没有hive-site,则依然走的是spark内置的Hive

5.运行org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver的main方法
(1)启动时将scope为Provided的依赖加上：Include dependencies with "Provided" scope这个选项勾选上
否则会报一些Provided scope的依赖找不到，常见的报错有：
java.lang.ClassNotFoundException: com.google.common.cache.CacheLoader
由这个依赖引起：
<dependency>
 <groupId>com.google.guava</groupId>
 <artifactId>guava</artifactId>
 <version>14.0.1</version>
 <scope>provided</scope>
 </dependency>
Exception in thread "main" java.lang.NoClassDefFoundError: org/eclipse/jetty/server/handler/ContextHandler
由groupId为org.eclipse.jetty的10个依赖中的某一个引起，可能是：
<dependency>
<groupId>org.eclipse.jetty</groupId>
<artifactId>jetty-util</artifactId>
<version>${jetty.version}</version>
<scope>provided</scope>
</dependency>
(2)VM options中添加-Dspark.master=local指定master
否则报错：Exception in thread "main" org.apache.spark.SparkException: A master URL must be set in your configuration
(3)Before launch中将build去掉，已经编译通过了，就不需要再启动前再build了
去掉build，否则耗时还可能造成编译不通过
(4)VM options中添加-Djline.WindowsTerminal.directConsole=false
不加这个，表现为输入一个sql(例如：show databases;)语句执行，不报错，但也不出结果，一直hang在那里

6.上一步启动时还会报错：Exception in thread "main" java.lang.NoClassDefFoundError: org/w3c/dom/ElementTraversal
参考：https://github.com/apache/spark/pull/4933
https://issues.apache.org/jira/browse/SPARK-6205
Exception in thread "main" java.lang.NoClassDefFoundError: org/w3c/dom/ElementTraversal
解决方法：修改父pom文件，把scope由test改为runtime，再重新刷新下maven依赖
<dependency>
  <groupId>xml-apis</groupId>
  <artifactId>xml-apis</artifactId>
  <version>1.4.01</version>
  <scope>runtime</scope>
</dependency>

7.可以正常启动并对接Hive了
```