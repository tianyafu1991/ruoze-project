package com.ruozedata.ruozesparksb.etl.access

import com.ruozedata.ruozesparksb.utils.{DateUtils, IpUtils}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation, TableScan}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SaveMode}

case class AccessRelation(override val sqlContext: SQLContext, path: String, userSchema: StructType)
  extends BaseRelation
    with InsertableRelation
    with TableScan {
  //BaseRelation要实现的方法
  override def schema: StructType = {
    if (userSchema != null) {
      userSchema
    } else {
      StructType(StructField("ip", StringType, false) ::
        StructField("proxyIp", StringType, true) ::
        StructField("responseTime", StringType, true) ::
        StructField("referer", StringType, true) ::
        StructField("method", StringType, true) ::
        StructField("url", StringType, true) ::
        StructField("httpCode", StringType, true) ::
        StructField("requestSize", StringType, true) ::
        StructField("responseSize", StringType, true) ::
        StructField("cache", StringType, true) ::
        StructField("uaHead", StringType, true) ::
        StructField("fileType", StringType, true) ::
        StructField("province", StringType, true) ::
        StructField("city", StringType, true) ::
        StructField("isp", StringType, true) ::
        StructField("http", StringType, true) ::
        StructField("domain", StringType, true) ::
        StructField("path", StringType, true) ::
        StructField("params", StringType, true) ::
        StructField("year", StringType, true) ::
        StructField("month", StringType, true) ::
        StructField("day", StringType, true) ::
        StructField("hour", StringType, true) ::
        StructField("minute", StringType, true) ::
        StructField("second", StringType, true) ::
        StructField("time", StringType, true) ::
        Nil)
    }
  }


  //TableScan要实现的方法
  override def buildScan(): RDD[Row] = {
    val lines: RDD[String] = sqlContext.sparkContext.textFile(path)
    val splits: RDD[Array[String]] = lines.map(_.split("\t").map(_.trim))

    splits.map(arr => {
      try {
        val time = arr(0)
        println(s"时间:${time}")
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
        val timeInfos = DateUtils.analysistime(time)
        val year = timeInfos(0)
        val month = timeInfos(1)
        val day = timeInfos(2)
        val hour = timeInfos(3)
        val minute = timeInfos(4)
        val second = timeInfos(5)
        val partitionTime = f"${year}${month}${day}${hour}"
        List(ip, proxyIp, responseTime, referer, method, url, httpCode, requestSize, responseSize, cache, uaHead, fileType, province, city, isp, http, domain, path, params, year, month, day, hour, minute, second, partitionTime)
      } catch {
        case e: Exception => {
          e.printStackTrace()
          null
        }
      }
    }).filter(_ != null).map(list => Row.fromSeq(list))
  }

  // InsertableRelation 要实现的方法
  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    data.write.mode(if (overwrite) SaveMode.Overwrite else SaveMode.Append).save(path)
  }
}
