package com.ruoze.bigdata.tututuhomework.day20201019.accessEtl

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession

object AccessETL2HBaseApp extends Logging {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().getOrCreate()

    val path = "data"
    val readFormat = "com.ruoze.bigdata.tututuhomework.day20201019.accessEtl.raw"
    spark.read.format(readFormat).load(path).show(100,true)


    spark.stop()
  }

}
