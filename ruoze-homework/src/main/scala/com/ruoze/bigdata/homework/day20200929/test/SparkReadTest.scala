package com.ruoze.bigdata.homework.day20200929.test

import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkReadTest {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().appName(this.getClass.getSimpleName).master("local").getOrCreate()
    val path = "F:\\ruozedata\\code\\ruoze-project\\ruoze-homework\\src\\main\\scala\\com\\ruoze\\bigdata\\homework\\day20200929\\data\\a"
    val df: DataFrame = spark
      .read
      .option("recursiveFileLookup", "true")
      .text(path)

    import spark.implicits._

    df.show(false)


    spark.stop()
  }

}
