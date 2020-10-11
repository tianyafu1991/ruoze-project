package com.ruoze.bigdata.homework.day20200929

import java.text.SimpleDateFormat

import com.ruoze.bigdata.homework.day20200929.utils.{DateUtils, FileUtils, IpUtils}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, RowFactory, SaveMode, SparkSession}

/**
 * spark sql读写数据的思路：
 *  采用spark sql自定义外部数据源读取raw log 然后使用自定义的外部数据源写出
 *  外部数据源读取是上课讲过的，作业也做过
 *  外部数据源写出参考：http://sparkdatasourceapi.blogspot.com/2016/10/spark-data-source-api-write-custom.html
 *
 * spark sql etl过程中的一些自定义需求的思路：
 *  1.partitionBy()进行分区
 *  2.写出要指定文件名，参考了http://cn.voidcc.com/question/p-hvctvrpy-uw.html，
 *  使用spark.sql.sources.commitProtocolClass参数设置为自定义的CommitProtocol，通过重写newTaskTempFile方法实现
 *  3.最终写到hdfs上要指定不同的分隔符，这个没找到好方法 但spark3.0已经支持了这个功能，将spark版本升级为3.0.1即可
 *  参考https://issues.apache.org/jira/browse/SPARK-24540
 *
 * 后续优化：
 * 目前虽然功能是实现了 但很多都是硬编码中写死的，比如数据写入hdfs时的文件名规则，后续优化可以将配置提到数据库中
 *
 * 上面的读写数据的思路中，用到了自定义的CommitProtocol来定义输出的目录和文件名，
 * 但也有弊端：
 *  在CommitProtocol中只能通过newTaskTempFile的参数taskContext获取到hadoop conf
 *  val configuration: Configuration = taskContext.getConfiguration
 *
 *  如果需要将一些自定义配置提到数据库中，spark将配置从数据库中读到，只能将配置写入到hadoop conf中，才能在自定义的CommitProtocol
 *  中拿到配置，这样就污染了hadoop conf
 *
 *  所以后续的优化思路就需要变成先将数据写出到hdfs，然后rename文件成想要的文件名
 *
 *
 */
object SparkSQLAccessLogApp {

  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "hadoop")

    val spark = SparkSession
      .builder()
      /*.appName(this.getClass.getSimpleName)
      .master("local")*/
      .config("spark.sql.sources.commitProtocolClass", "com.ruoze.bigdata.homework.day20200929.commitProtocol.MyHadoopMapReduceCommitProtocol")
      .getOrCreate()

    val customTimeFormat = "yyyyMMddHH"

    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", "hdfs://hadoop:9000")

    val path = "/ruozedata/data/access.txt"

    val outPut = "/ruozedata/output1"
    val outPut2 = "/ruozedata/log"

    /*val path = "ruoze-homework/src/main/scala/com/ruoze/bigdata/homework/day20200929/data/access.txt"
    val outPut = "ruoze-homework/src/main/resources/out"
    val outPut2 = "ruoze-homework/src/main/resources/out2"*/



    FileUtils.delete(spark.sparkContext.hadoopConfiguration, outPut)
    FileUtils.delete(spark.sparkContext.hadoopConfiguration, outPut2)

    val rawDataSource = "com.ruoze.bigdata.homework.day20200929.rawETL"

    val accessDF: DataFrame = spark.read.format(rawDataSource).load(path)

    accessDF
      .write
      .mode(SaveMode.Overwrite)
      .options(Map("format" -> "customFormat"))
      .format(rawDataSource)
      .save(outPut)


    spark.conf.unset("spark.sql.sources.commitProtocolClass")
    spark.conf.set("spark.sql.sources.commitProtocolClass", "com.ruoze.bigdata.homework.day20200929.commitProtocol.MyHadoopMapReduceCommitProtocol2")

    val format = "com.ruoze.bigdata.homework.day20200929.upload2HDFS"

    val inputDF: DataFrame = spark.read.format(format).load(outPut)

    inputDF
      .write
      .format(format)
      .mode(SaveMode.Append)
      .save(outPut2)

    spark.stop()
  }

}
