package com.ruoze.bigdata.tututuhomework.day20201130

import org.apache.spark.{SparkConf, SparkContext}

/*trait Template {

  def init()

  def biz()

  def stop()

  try{
    init()
    biz()
  }catch {
    case e:Exception => {
      println()
    }
  }finally {
    stop()
  }

}*/

trait Template {

  def execute(): Unit = {
    val conf:SparkConf = new SparkConf().setAppName(getClass.getCanonicalName).setMaster("local[2]")
    val sc:SparkContext = new SparkContext(conf)



    sc.stop()
  }

}
