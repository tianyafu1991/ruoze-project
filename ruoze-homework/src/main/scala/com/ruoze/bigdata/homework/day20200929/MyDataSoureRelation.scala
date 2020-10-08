package com.ruoze.bigdata.homework.day20200929

import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation, TableScan}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

class MyDataSoureRelation(@transient val sqlContext: SQLContext, path: String,udfSchema:StructType)
  extends BaseRelation
    with TableScan
    with InsertableRelation
    with Logging {

  override def schema: StructType = {
    if(null != udfSchema){
      udfSchema
    }else{
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
        StructField("time", IntegerType, true) ::
        StructField("domain", StringType, true) ::
        Nil)
    }
  }

  override def buildScan(): RDD[Row] = {
    logError("这是天涯自定义的数据源实现：buildScan")
    val spark = sqlContext.sparkSession
    import spark.implicits._
    val inputDF =spark.read
      .format("csv")
      .option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ")
      .load(path)
    inputDF.printSchema()
    inputDF.show()

    inputDF.show()
    inputDF.rdd
//    null
  }

  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    data.write.mode(if (overwrite) SaveMode.Overwrite else SaveMode.Append).save(path)
  }
}
