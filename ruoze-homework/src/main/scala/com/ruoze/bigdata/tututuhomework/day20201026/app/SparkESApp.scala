package com.ruoze.bigdata.tututuhomework.day20201026.app

import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.elasticsearch.spark.sql._

/**
 * spark对接es
 * 官方文档：https://www.elastic.co/guide/en/elasticsearch/hadoop/7.9/spark.html
 * maven GAV:https://www.elastic.co/guide/en/elasticsearch/hadoop/current/install.html#download-dev
 * jar包下载：https://www.elastic.co/cn/downloads/hadoop 需要手动安装到本地仓库
 *
 * https://www.cnblogs.com/upupfeng/p/12205657.html
 */
object SparkESApp {


  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
    val spark = SparkSession.builder().config(conf).getOrCreate()
    print(spark)
    System.setProperty("HADOOP_USER_NAME", "hadoop")
    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS", s"hdfs://hadoop01:9000")

    val filePath = conf.get("spark.dw.raw.path", "/ruozedata/dw/raw/hbase")

    val executionTime: String = conf.get("spark.execute.time", "20190101")
    val input = s"${filePath}/${executionTime}"

    val rawFormat = "com.ruoze.bigdata.tututuhomework.day20201022.rawIo"

    val rawDF = spark.read.format(rawFormat).load(input)

//    rawDF.show(10,false)

    val esOptions = Map(
      "es.nodes"->"hadoop01",
      "es.port" -> "9200"
    )

//    rawDF.saveToEs()


    /*//DF读写ES
    rawDF
      .write
      .format("es")
      .options(esOptions)
      .mode(SaveMode.Overwrite)
      .save("access_log/")


    val esDF = spark.read.format("es").options(esOptions).load("access_log/")

    esDF.show(false)*/



    spark.stop()

  }

}
