package com.ruoze.flink.source

import com.ruoze.flink.bean.Domain.Access
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object SourceApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(4)

    val stream = env.fromCollection(List(
      Access(202112120010L, "ruozedata.com", 2000),
      Access(202112120010L, "ruoze.ke.qq.com", 6000),
      Access(202112120010L, "github.com/ruozedata", 5000),
      Access(202112120010L, "ruozedata.com", 4000),
      Access(202112120010L, "ruoze.ke.qq.com", 1000)
    ))
    println("-------"+stream.parallelism)

    val filterStream = stream.filter(_.traffic > 4000)

    println("-------"+filterStream.parallelism)

    env.execute(getClass.getCanonicalName)
  }

}
