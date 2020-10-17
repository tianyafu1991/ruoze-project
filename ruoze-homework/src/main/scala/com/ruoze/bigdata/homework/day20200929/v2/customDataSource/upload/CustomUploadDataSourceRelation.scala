package com.ruoze.bigdata.homework.day20200929.v2.customDataSource.upload

import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SaveMode}
import org.apache.spark.sql.sources.{BaseRelation, InsertableRelation, RelationProvider, TableScan}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

class CustomUploadDataSourceRelation(@transient val sqlContext:SQLContext,path:String,userSchema:StructType)
  extends BaseRelation
  with TableScan
  with InsertableRelation
  with Logging{

  //BaseRelation
  override def schema: StructType = {
      if(userSchema != null){
        userSchema
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
    null
  }

  //InsertableRelation
  override def insert(data: DataFrame, overwrite: Boolean): Unit = {
    data.write.mode(if(overwrite) SaveMode.Overwrite else SaveMode.Append).save(path)
  }
}
