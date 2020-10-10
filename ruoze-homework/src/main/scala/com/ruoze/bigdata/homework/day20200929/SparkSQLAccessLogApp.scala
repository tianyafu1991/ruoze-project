package com.ruoze.bigdata.homework.day20200929

import java.text.SimpleDateFormat

import com.ruoze.bigdata.homework.day20200929.utils.{DateUtils, FileUtils, IpUtils}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, RowFactory, SaveMode, SparkSession}

/**
 * http://cn.voidcc.com/question/p-hvctvrpy-uw.html
 *
 * http://sparkdatasourceapi.blogspot.com/2016/10/spark-data-source-api-write-custom.html
 */
object SparkSQLAccessLogApp {

  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "hadoop")

    val spark = SparkSession
      .builder()
      /*.appName(this.getClass.getSimpleName)
      .master("local")*/
      .config("spark.sql.sources.commitProtocolClass", "com.ruoze.bigdata.homework.day20200929.MyHadoopMapReduceCommitProtocol")
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
    spark.conf.set("spark.sql.sources.commitProtocolClass", "com.ruoze.bigdata.homework.day20200929.MyHadoopMapReduceCommitProtocol2")

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
