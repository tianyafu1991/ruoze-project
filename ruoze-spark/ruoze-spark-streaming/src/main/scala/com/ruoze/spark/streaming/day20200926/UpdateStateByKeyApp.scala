package com.ruoze.spark.streaming.day20200926

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object UpdateStateByKeyApp {

  def updateFunc(newValue:Seq[Int],oldValue:Option[Int]): Option[Int] = {

    val newCount = newValue.sum
    val pre = oldValue.getOrElse(0)
    Some(newCount+pre)
  }

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(conf, Seconds(5))

    ssc.checkpoint("chk")

    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("hadoop01", 9527)

    lines.flatMap(_.split(",")).map((_,1)).updateStateByKey(updateFunc).print()



    ssc.start()
    ssc.awaitTermination()
  }


}
