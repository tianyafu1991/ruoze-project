package com.ruoze.bigdata.homework.day20200929

import com.ruoze.bigdata.homework.day20200929.utils.FileUtils
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

/**
 * http://cn.voidcc.com/question/p-hvctvrpy-uw.html
 *
 * http://sparkdatasourceapi.blogspot.com/2016/10/spark-data-source-api-write-custom.html
 *
 * spark3.0支持多分隔符
 * https://issues.apache.org/jira/browse/SPARK-24540
 */
object SparkSQLAccessLogApp2 {

  def main(args: Array[String]): Unit = {
    //    System.setProperty("HADOOP_USER_NAME", "hadoop")

    val spark = SparkSession
      .builder()
      .appName(this.getClass.getSimpleName)
      .master("local")
      .config("spark.sql.sources.commitProtocolClass", "com.ruoze.bigdata.homework.day20200929.commitProtocol.MyHadoopMapReduceCommitProtocol2")
      .getOrCreate()

    val customTimeFormat = "yyyyMMddHH"

    //    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", "hdfs://hadoop01:9000")

    /*val path = "/ruozedata/data/access.txt"

    val outPut = "/ruozedata/output1"
    val outPut2 = "/ruozedata/log"*/


    //    val input = "ruoze-homework/src/main/resources/out"
    val input = "ruoze-homework/src/main/resources/out/"
    val output = "ruoze-homework/src/main/resources/out2"



    //    FileUtils.delete(spark.sparkContext.hadoopConfiguration, outPut)
    FileUtils.delete(spark.sparkContext.hadoopConfiguration, output)

    val format = "com.ruoze.bigdata.homework.day20200929.upload2HDFS"

    val inputDF: DataFrame = spark.read.format(format).load(input)

    inputDF.show(truncate = false)
    inputDF.printSchema()

    inputDF
      .write
      .format(format)
      .mode(SaveMode.Append)
      .save(output)

    spark.stop()
  }

}
