package com.ruoze.spark.streaming.day20201014.hbase

object HBaseDataSourceUtils {

  /**
   * schema (age int,name string,sex string)
   * @param schema
   * @return
   */
  def extractSparkFields(schema:String):Array[SparkSchema]={
    schema.trim.drop(1).dropRight(1).split(",").map(column => {
      val splits: Array[String] = column.trim.split(" ")
      SparkSchema(splits(0),splits(1))
    })
  }

  def main(args: Array[String]): Unit = {
    extractSparkFields("(age int,name string,sex string)").foreach(println)
  }

}
