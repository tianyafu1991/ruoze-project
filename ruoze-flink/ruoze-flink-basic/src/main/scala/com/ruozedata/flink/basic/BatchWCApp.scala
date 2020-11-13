package com.ruozedata.flink.basic

import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.api.scala._

object BatchWCApp {

  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment

    val text: DataSet[String] = env.readTextFile("data/wc.txt")

    text.flatMap(_.toLowerCase.split(","))
      .filter(_.nonEmpty)
      .map((_,1))
      .groupBy(0)
      .sum(1)
      .print()

//    env.execute(getClass.getCanonicalName)
  }
}
