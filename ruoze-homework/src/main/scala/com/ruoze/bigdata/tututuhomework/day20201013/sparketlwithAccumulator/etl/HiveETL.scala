package com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.etl

import org.apache.spark.sql.SparkSession

object HiveETL {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().enableHiveSupport().getOrCreate()




    spark.stop()
  }

}
