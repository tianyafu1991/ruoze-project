package com.ruoze.bigdata.homework.day20200929.v1.upload2HDFS

import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation, TableScan}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

class UploadDataSourceRelation(override val sqlContext: SQLContext, path: String, userSchema: StructType)
  extends BaseRelation
    with TableScan
    with InsertableRelation
    with Logging {
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
        StructField("path", StringType, true) ::
        StructField("params", StringType, true) ::
        StructField("year", StringType, true) ::
        StructField("month", StringType, true) ::
        StructField("day", StringType, true) ::
        StructField("hour", StringType, true) ::
        StructField("time", StringType, true) ::
        StructField("domain", StringType, true) ::
        Nil)
    }
  }

  //TableScan要实现的方法
  override def buildScan(): RDD[Row] = {
    var allFilePath = path +"/*/*"
    val lineRDD: RDD[String] = sqlContext.sparkContext.textFile(allFilePath)
//    val lineRDD: RDD[String] = sqlContext.sparkContext.wholeTextFiles(allFilePath).map(_._2)
    val splits: RDD[Array[String]] = lineRDD.map(_.split(",").map(_.trim))
    splits.map(arr => {
      val ip = arr(0)
      val proxyIp = arr(1)
      val responseTime = arr(2)
      val referer = arr(3)
      val method = arr(4)
      val url = arr(5)
      val httpCode = arr(6)
      val requestSize = arr(7)
      val responseSize = arr(8)
      val cache = arr(9)
      val uaHead = arr(10)
      val fileType = arr(11)
      val province = arr(12)
      val city = arr(13)
      val isp = arr(14)
      val http = arr(15)
      val path = arr(16)
      val params = arr(17)
      val year = arr(18)
      val month = arr(19)
      val day = arr(20)
      val hour = arr(21)
      val myTime = arr(22)
      val myDomain = arr(23)
//      val date = year+month+day
//      val time = arr(24)
//      val domain = arr(25)
//      List(ip,proxyIp,responseTime,referer,method,url,httpCode,requestSize,responseSize,cache,uaHead,fileType,province,city,isp,http,path,params,year,month,day,hour,myTime,myDomain,time,domain)
      List(ip,proxyIp,responseTime,referer,method,url,httpCode,requestSize,responseSize,cache,uaHead,fileType,province,city,isp,http,path,params,year,month,day,hour,myTime,myDomain)
    }).map(x => Row.fromSeq(x))
  }

  //InsertableRelation要实现的方法
  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    data.write.mode(if(overwrite) SaveMode.Overwrite else SaveMode.Append).save(path)
  }
}
