package com.ruoze.bigdata.homework.day20200929

import java.text.SimpleDateFormat

import com.ruoze.bigdata.homework.day20200929.utils.{DateUtils, FileUtils, IpUtils}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, RowFactory, SparkSession}

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
      .appName(this.getClass.getSimpleName)
      .master("local")
      .config("spark.sql.sources.commitProtocolClass", "com.ruoze.bigdata.homework.day20200929.MyHadoopMapReduceCommitProtocol")
      .getOrCreate()

    val customTimeFormat = "yyyyMMddHH"

    import spark.implicits._

    spark.sparkContext.hadoopConfiguration.set("fs.defaultFS","hdfs://hadoop01:9000")

    val path = "/ruozedata/data/access2.txt"

    val outPut = "/ruozedata/output1"
    val outPut2 = "/ruozedata/log"

    FileUtils.delete(spark.sparkContext.hadoopConfiguration, outPut)
    FileUtils.delete(spark.sparkContext.hadoopConfiguration, outPut2)

    val lines: RDD[String] = spark.sparkContext.textFile(path)

    val splits: RDD[Array[String]] = lines.map(_.split("\t").map(_.trim))

    val accessRDD: RDD[Access] = splits.map(arr => {
      try {
        val time = arr(0)
        val ip = arr(1)
        val proxyIp = arr(2)
        val responseTime = arr(3)
        val referer = arr(4)
        val method = arr(5)
        val url = arr(6)
        val httpCode = arr(7)
        val requestSize = arr(8)
        val responseSize = arr(9)
        if ("-".equals(responseSize)) {
          throw new IllegalAccessException("")
        }
        val cache = arr(10)
        val uaHead = arr(11)
        val fileType = arr(12)
        //解析ip
        val ipInfos = IpUtils.analysisIp(ip)
        val province = ipInfos(2)
        val city = ipInfos(3)
        val isp = ipInfos(4)


        //解析url
        val urlSplits = url.split("\\?")
        val urlSplits2 = urlSplits(0).split(":")

        val http = urlSplits2(0)
        val urlSpliting = urlSplits2(1).substring(2)
        var domain = urlSpliting
        var path = ""
        if (urlSpliting.contains("/")) {
          domain = urlSpliting.substring(0, urlSpliting.indexOf("/"))
          path = urlSpliting.substring(urlSpliting.indexOf("/"))
        }
        val params = if (urlSplits.length == 2) urlSplits(1) else null
        //解析time
        val simpleDateFormat: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy:hh:mm:ss")
        val timeInfos = DateUtils.analysistime(time, simpleDateFormat)
        val year = timeInfos(0)
        val month = timeInfos(1)
        val day = timeInfos(2)
        val hour = timeInfos(3)

        val customTime = DateUtils.customFormatTime(time, customTimeFormat)
        Access(ip, proxyIp, responseTime.toLong, referer, method, url, httpCode, requestSize.toLong, responseSize.toLong, cache, uaHead, fileType, province, city, isp, http, domain, path, params, year, month, day, hour,customTime)
      } catch {
        case e: Exception => {
          e.printStackTrace()
          null
        }
      }
    }).filter(null != _)


    val accessDF: DataFrame = accessRDD.toDF()
//    accessDF.show(20)
//    accessDF.printSchema()

    val keDF: Dataset[Row] = accessDF.filter('domain === "ruoze.ke.qq.com")
    val ruozeDF: Dataset[Row] = accessDF.filter('domain === "ruozedata.com")

    accessDF.cache()

    accessDF.write
      .partitionBy("time", "domain")
      .option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ")
//      .option("compression","bzip2")
      .format("csv")
      .save(outPut)


    spark.conf.set("spark.sql.sources.commitProtocolClass","com.ruoze.bigdata.homework.day20200929.MyHadoopMapReduceCommitProtocol2")

    val format = "com.ruoze.bigdata.homework.day20200929"

    val inputDF: DataFrame = spark.read.format(format).load(outPut)

    inputDF
      .write
      .partitionBy("time","domain")
      .option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ")
      .option("format","customFormat")
      .format(format).save(outPut2)



    spark.stop()
  }

}

case class Access(ip: String,
                  proxyIp: String,
                  responseTime: Long,
                  referer: String,
                  method: String,
                  url: String,
                  httpCode: String,
                  requestSize: Long,
                  responseSize: Long,
                  cache: String,
                  uaHead: String,
                  fileType: String,
                  province: String,
                  city: String,
                  isp: String,
                  http: String,
                  domain: String,
                  path: String,
                  params: String,
                  year: String,
                  month: String,
                  day: String,
                  hour: String,
                  time: String)
