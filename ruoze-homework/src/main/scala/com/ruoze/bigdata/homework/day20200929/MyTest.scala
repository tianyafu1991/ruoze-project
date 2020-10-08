package com.ruoze.bigdata.homework.day20200929

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object MyTest {

  def main(args: Array[String]): Unit = {
    val outPut = "F:\\study\\ruozedata\\review\\ruozeg9\\ruozedata-homework\\src\\main\\java\\com\\tianya\\bigdata\\homework\\day20200929\\out"

    val spark = SparkSession.builder().master("local").appName(this.getClass.getSimpleName).getOrCreate()
    import spark.implicits._
    val inputDF: DataFrame = spark.read.format("csv")
      .option("enforceSchema","false")
      .option("inferSchema","false")
      .option("timestampFormat", "yyyy/MM/dd HH:mm:ss ZZ")
      .load(outPut)

//    val timeDF: Dataset[String] = inputDF.map(row => row.getAs[String]("time"))

    inputDF.printSchema()
    inputDF.show()





    spark.stop()
  }

}
