package com.ruoze.spark.streaming.day20200927

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StateApp3 extends Logging{

  def main(args: Array[String]): Unit = {

    val checkpoint = "chk2"

    def functionToCreateContext(): StreamingContext = {
      val sparkConf = new SparkConf()
        .setAppName(this.getClass.getCanonicalName).setMaster("local[2]")
      val ssc = new StreamingContext(sparkConf, Seconds(5))   // new context
      ssc.checkpoint(checkpoint)

      ssc
    }

    val ssc = StreamingContext.getActiveOrCreate(checkpoint, functionToCreateContext)
    val lines = ssc.socketTextStream("hadoop01", 9527) // create DStreams
    val result = lines.flatMap(_.split(","))
      .map((_,1)).updateStateByKey(updateFunction)

    result.print()

    ssc.start()
    ssc.awaitTermination()

  }

  def updateFunction(newValues: Seq[Int], preValues: Option[Int]): Option[Int] = {
    val newCount = newValues.sum
    val pre = preValues.getOrElse(0)
    Some(newCount + pre)
  }

}
