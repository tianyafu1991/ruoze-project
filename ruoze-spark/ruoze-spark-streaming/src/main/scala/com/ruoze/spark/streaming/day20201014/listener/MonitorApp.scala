package com.ruoze.spark.streaming.day20201014.listener

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object MonitorApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("MonitorApp_20201022")
      .set("spark.enable.send.mail","true")
      .set("spark.extraListeners","com.ruoze.spark.streaming.day20201014.listener.RuozedataSparkAppListener")

    val sc = new SparkContext(conf)

    val rdd1: RDD[Int] = sc.parallelize(List(1, 2, 3, 4, 5))

    rdd1.foreach(println)



    sc.stop()
  }

}
