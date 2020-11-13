package com.ruoze.flink.bykey

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object SepcifingTranformationFunctionsApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.readTextFile("data/access.txt").print()



    env.execute(getClass.getCanonicalName)

  }

}
