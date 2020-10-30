package com.ruozedata.recommend.app

import java.time.LocalDate

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object OrderApp extends Logging {


  def main(args: Array[String]): Unit = {

    System.setProperty("HADOOP_USER_NAME", "hadoop")

    val conf = new SparkConf()
    val spark: SparkSession = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate()


    val config: Config = ConfigFactory.load()
    val database: String = config.getString("database")
    val ordersTable: String = config.getString("ordersTable")
    val url: String = config.getString("url")
    val username: String = config.getString("username")
    val password: String = config.getString("password")

    logError("加载订单数据......")
    val member: DataFrame = spark.read.format("jdbc")
      .option(JDBCOptions.JDBC_URL, url)
      .option(JDBCOptions.JDBC_TABLE_NAME, s"${database}.${ordersTable}")
      .option("user", username)
      .option("password", password)
      .load()


    member.createOrReplaceTempView("orders")


    memberOrderStat

    def memberOrderStat: Unit = {
      logError("订单统计........")
      val end = LocalDate.of(2022,7,26)
      val start = end.minusDays(14L)
      val sql =
        s"""
          |select
          |date_format(create_time,'yyyy-MM-dd') dt,
          |count(1) order_cnts
          |from
          |orders
          |where create_time >= '${start}' and create_time <= '${end}'
          |group by
          |date_format(create_time,'yyyy-MM-dd')
          |""".stripMargin

      val result: DataFrame = spark.sql(sql)

      result
        .write
        .mode(SaveMode.Overwrite)
        .format("hive")
        .option("fileFormat", "orc")
        .option("compression", "zlib")
        .saveAsTable(config.getString("order.app.output"))

      logError("订单统计写入Hive。。。。。。。")

      spark.table(config.getString("order.app.output"))
        .write.mode(SaveMode.Overwrite)
        .option(JDBCOptions.JDBC_URL, config.getString("target.url"))
        .option(JDBCOptions.JDBC_TABLE_NAME, config.getString("mysql.order.table"))
        .option("user", username)
        .option("password", password)
        .format("jdbc")
        .save()

      logError("订单统计插入MySQL..........")
    }




    spark.stop()
  }

}
