package com.ruoze.bigdata.tututuhomework.day20201026.app

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}
/*import org.elasticsearch.spark.sql._
import org.elasticsearch.spark._*/
import org.json4s.DefaultFormats
import org.json4s.jackson.Json

/**
 * spark对接es
 * 官方文档：https://www.elastic.co/guide/en/elasticsearch/hadoop/7.9/spark.html
 * maven GAV:https://www.elastic.co/guide/en/elasticsearch/hadoop/current/install.html#download-dev
 * jar包下载：https://www.elastic.co/cn/downloads/hadoop 需要手动安装到本地仓库
 *
 * https://www.cnblogs.com/upupfeng/p/12205657.html
 */
object SparkESApp {

  /*
    def main(args: Array[String]): Unit = {

      val conf = new SparkConf()
      val spark = SparkSession.builder().config(conf).getOrCreate()
      val sc: SparkContext = spark.sparkContext

      System.setProperty("HADOOP_USER_NAME", "hadoop")
      sc.hadoopConfiguration.set("fs.defaultFS", "hdfs://hadoop:9000")

      val filePath = conf.get("spark.dw.raw.path", "/ruozedata/dw/raw/hbase")
      val flag = conf.get("spark.use.df", "false").toBoolean
      val esNodes: String = conf.get("spark.es.nodes", "hadoop")
      val esPort: String = conf.get("spark.es.port", "9200")
      val resource: String = conf.get("spark.es.resource", "access_log_rdd/")

      val executionTime: String = conf.get("spark.execute.time", "20190101")
      val input = s"${filePath}/${executionTime}"

      val rawFormat = "com.ruoze.bigdata.tututuhomework.day20201022.rawIo"

      val esOptions: Map[String, String] = Map(
        "es.nodes" -> esNodes,
        "es.port" -> esPort
      )


      val rawDF = spark.read.format(rawFormat).load(input)

      rawDF.show(10, false)
      println("这个是从文本中取出来的")


      if (!flag) {
        //使用RDD
        saveJSONToESUseRDD(resource, esOptions, rawDF.toJSON.rdd)
        println("使用RDD方式写入ES成功。。。。。。。。。。")
        val esRDD: RDD[(String, collection.Map[String, AnyRef])] = sc.esRDD(resource, esOptions)
        esRDD.foreach(println)
        println("从ES中读取完毕")
      } else {
        //DF读写ES
        rawDF
          .write
          .format("es")
          .options(esOptions)
          .mode(SaveMode.Overwrite)
          .save(resource)

        val esDF = spark
          .read
          .format("es")
          .options(esOptions)
          .load(resource)

        esDF.show(false)
      }

      spark.stop()

    }*/


  /*def saveJSONToESUseRDD(resource: String, esOptions: Map[String, String], jsonRDD: RDD[String]): Unit = {

    jsonRDD.saveJsonToEs(resource, esOptions)
  }


  def saveToESDemo(sc: SparkContext, esOptions: Map[String, String]): Unit = {
    val json1 = """{"reason" : "business", "airport" : "SFO"}"""
    val json2 = """{"participants" : 5, "airport" : "OTP"}"""

    val esDemoRDD: RDD[String] = sc.makeRDD(Seq(json1, json2))
    esDemoRDD.foreach(println)
    //    esDemoRDD.saveJsonToEs("spark/json-trips",esOptions)
  }*/

}
