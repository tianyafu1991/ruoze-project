package com.ruoze.spark.streaming.day20201014.hbase

import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkReadHBaseApp {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().getOrCreate()


    val format = "com.ruoze.spark.streaming.day20201014.hbase"

    val data: DataFrame = spark.read
      .format(format)
      .option("hbase.table", "user")
      .option("spark.table.schema", "(age int,name string,sex string)")
      .load()

    /*data.printSchema
    data.show(false)
*/

    data.createOrReplaceTempView("user")
    val sql =
      """
        |select * from user where age < 25
        |""".stripMargin
    spark.sql(sql).show()

    spark.stop()
  }

}
