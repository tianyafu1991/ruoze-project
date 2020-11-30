package com.ruoze.bigdata.homework.day20201127

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object CsvApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName(getClass.getCanonicalName).setMaster("local[2]")
    val spark = SparkSession.builder().config(conf).getOrCreate()
    val filePath = "file:///F:/study/ruozedata/ruoze-project/ruoze-homework/src/main/scala/com/ruoze/bigdata/homework/day20201127/123.csv"
    val csvDF = spark.read.format("csv")
      .option("header", "true")
      .option("inferSchema", value = false)
      .option("nullValue", "\\N")
      .option("sep", ",")
      .option("quote", "\"")
      .option("escape", "\"")
      .option("multiLine", "true")
      .load(filePath)

    csvDF.show(truncate = false)

    spark.close()


  }

}
