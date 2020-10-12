package com.ruoze.spark.streaming.day20200926

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, State, StateSpec, StreamingContext}

object MapWithStateApp {

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

    val mappingFunc = (word: String, one: Option[Int], state: State[Int]) => {
      val sum = one.getOrElse(0) + state.getOption.getOrElse(0)
      val output = (word, sum)
      state.update(sum)
      output
    }

    lines.flatMap(_.split(",")).map((_,1)).mapWithState(
      StateSpec.function(mappingFunc).timeout(Seconds(100)))



    ssc.start()
    ssc.awaitTermination()
  }




}
