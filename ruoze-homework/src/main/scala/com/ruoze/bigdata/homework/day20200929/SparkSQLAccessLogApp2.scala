package com.ruoze.bigdata.homework.day20200929

import com.ruoze.bigdata.homework.day20200929.utils.FileUtils
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * http://cn.voidcc.com/question/p-hvctvrpy-uw.html
 *
 * http://sparkdatasourceapi.blogspot.com/2016/10/spark-data-source-api-write-custom.html
 */
object SparkSQLAccessLogApp2 {

  def main(args: Array[String]): Unit = {
    //    System.setProperty("HADOOP_USER_NAME", "hadoop")

    val spark = SparkSession
      .builder()
      .appName(this.getClass.getSimpleName)
      .master("local")
      //      .config("spark.sql.sources.commitProtocolClass", "com.ruoze.bigdata.homework.day20200929.MyHadoopMapReduceCommitProtocol")
      .getOrCreate()

    val customTimeFormat = "yyyyMMddHH"

    //    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", "hdfs://hadoop01:9000")

    /*val path = "/ruozedata/data/access2.txt"

    val outPut = "/ruozedata/output1"
    val outPut2 = "/ruozedata/log"*/

    val outPut = "F:\\study\\ruozedata\\ruoze-project\\ruoze-homework\\src\\main\\scala\\com\\ruoze\\bigdata\\homework\\day20200929\\out"
    val path = outPut
    val outPut2 = "F:\\study\\ruozedata\\ruoze-project\\ruoze-homework\\src\\main\\scala\\com\\ruoze\\bigdata\\homework\\day20200929\\out2"



    //    FileUtils.delete(spark.sparkContext.hadoopConfiguration, outPut)
    FileUtils.delete(spark.sparkContext.hadoopConfiguration, outPut2)

    val inputDF: DataFrame = spark.read.format("csv").option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ").load(path)

    inputDF.show()
    inputDF.printSchema()

    spark.stop()
  }

}
