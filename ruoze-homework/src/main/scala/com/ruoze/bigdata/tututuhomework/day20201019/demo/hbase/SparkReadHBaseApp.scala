package com.ruoze.bigdata.tututuhomework.day20201019.demo.hbase

import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkReadHBaseApp {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().getOrCreate()

    val tableSchema = "(ip string,proxyIp string,responseTime string,referer string,method string,url string,httpCode string,requestSize string,responseSize string,cache string,uaHead string,fileType string,province string,city string,isp string,http string,domain string,path string,params string,year string,month string,day string,hour string,minute string,second string,time string)"

    val format = "com.ruoze.bigdata.tututuhomework.day20201019.demo.hbase"

    val data: DataFrame = spark.read
      .format(format)
      .option("hbase.table", "ruozedata:access_log")
      .option("spark.table.schema", tableSchema)
      .load()

    data.printSchema
    data.show(false)

    /*data.createOrReplaceTempView("user")
    val sql =
      """
        |select * from user where age < 25
        |""".stripMargin
    spark.sql(sql).show()*/

    spark.stop()
  }

}
