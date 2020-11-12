package com.ruoze.bigdata.homework.day20201021.utils

import java.util
import java.util.Date

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions

import scala.collection.mutable.ListBuffer

object BroadcastUtils {
  var lastUpdateTimeStamp: Long = 0
  var updatedBroadcast: Broadcast[List[String]] = _
  def updateBroadcastValue(spark: SparkSession, broadcast: Broadcast[List[String]]): Broadcast[List[String]] = {
    val url = "jdbc:mysql://hadoop01:3306/ruozedata?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8"
    val user = "root"
    val password = "root"
    val table = "prewarning_config"
    val nowTimeStamp = new Date().getTime
    var keywordList:ListBuffer[String] = ListBuffer()

    if (null == updatedBroadcast || null == broadcast  ||nowTimeStamp - lastUpdateTimeStamp >= 60000) {
      if (null != broadcast) {
        broadcast.unpersist()
      }
      lastUpdateTimeStamp = new Date().getTime
      val prewarningConfigDF: DataFrame = spark.read
        .format("jdbc")
        .option(JDBCOptions.JDBC_URL, url)
        .option(JDBCOptions.JDBC_TABLE_NAME, table)
        .option(JDBCOptions.JDBC_DRIVER_CLASS, "com.mysql.jdbc.Driver")
        .option("user", user)
        .option("password", password)
        .load()


      val rows: util.List[Row] = prewarningConfigDF.collectAsList()
      for (i <- 0 until rows.size()) {
        val row: Row = rows.get(i)
        val keywords: String = row.getAs[String]("keywords")
        keywordList = keywordList :+ keywords
      }

      updatedBroadcast = spark.sparkContext.broadcast(keywordList.toList)
    }else {
      updatedBroadcast = broadcast
    }

    updatedBroadcast
  }

  def getTimeStamp(): Long = {
    new Date().getTime
  }

  def main(args: Array[String]): Unit = {
    println(getTimeStamp)
  }


}


