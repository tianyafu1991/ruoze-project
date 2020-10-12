package com.ruoze.spark.streaming.day20200923

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingSocketWCApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(this.getClass.getSimpleName).setMaster("local[2]")
    val ssc = new StreamingContext(conf, Seconds(5))

    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("dsf", 9527)
    lines.flatMap(_.split(" ")).map((_,1)).reduceByKey(_+_).print()


    ssc.start()
    ssc.awaitTermination()

  }

}
