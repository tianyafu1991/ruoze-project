package com.ruoze.spark.streaming.day20200927

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StatefulApp3 {

  def updateFunc(newValue:Seq[Int],oldValue:Option[Int]): Option[Int] = {

    val newCount = newValue.sum
    val pre = oldValue.getOrElse(0)
    Some(newCount+pre)
  }


  def main(args: Array[String]): Unit = {

    val checkpoint = "chk2"

    def functionToCreateContext(): StreamingContext = {
      val conf = new SparkConf().setMaster("local[3]").setAppName(getClass.getSimpleName)
      val ssc = new StreamingContext(conf, Seconds(5)) // new context
      ssc.checkpoint(checkpoint) // set checkpoint directory
      val lines = ssc.socketTextStream("hadoop01", 9527) // create DStreams
      lines.flatMap(_.split(",")).map((_, 1)).updateStateByKey(updateFunc).print()
      ssc
    }

    val ssc = StreamingContext.getOrCreate(checkpoint, functionToCreateContext)

    ssc.start()
    ssc.awaitTermination()
  }

}
