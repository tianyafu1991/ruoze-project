package com.ruoze.bigdata.homework.day20200929.v2.customDataSource.upload

import com.ruoze.bigdata.homework.day20200929.v2.implicits.RichSparkContext
import org.apache.spark.SparkContext
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation, RelationProvider, TableScan}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

class CustomUploadDataSourceRelation(@transient val sqlContext: SQLContext, path: String, userSchema: StructType)
  extends BaseRelation
    with TableScan
    with InsertableRelation
    with Logging {

  //BaseRelation
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

  //TableScan
  override def buildScan(): RDD[Row] = {
    implicit def sc2RichSc(sc: SparkContext): RichSparkContext = new RichSparkContext(sc)

    val sc: SparkContext = sqlContext.sparkContext
    val lines: RDD[String] = sc.recursiveTextFile(path)

    val splits: RDD[Array[String]] = lines.map(_.split("\t").map(_.trim))

    splits.map(arr => {
      val ip: String = arr(0)
      val proxyIp: String = arr(1)
      val responseTime: String = arr(2)
      val referer: String = arr(3)
      val method: String = arr(4)
      val url: String = arr(5)
      val httpCode: String = arr(6)
      val requestSize: String = arr(7)
      val responseSize: String = arr(8)
      val cache: String = arr(9)
      val uaHead: String = arr(10)
      val fileType: String = arr(11)
      val province: String = arr(12)
      val city: String = arr(13)
      val isp: String = arr(14)
      val http: String = arr(15)
      val domain: String = arr(16)
      val logPath: String = arr(17)
      val params: String = arr(18)
      val year: String = arr(19)
      val month: String = arr(20)
      val day: String = arr(21)
      val hour: String = arr(22)
      val minute: String = arr(23)
      val second: String = arr(24)
      val time: String = arr(25)
      List(ip, proxyIp, responseTime, referer, method, url, httpCode, requestSize
        , responseSize, cache, uaHead, fileType, province, city, isp, http, domain, logPath, params, year, month, day
        , hour, minute, second, time)
    }).map(Row.fromSeq(_))
  }

  //InsertableRelation
  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    data.write.mode(if (overwrite) SaveMode.Overwrite else SaveMode.Append).save(path)
  }
}
