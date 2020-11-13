package com.ruoze.flink.source

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.util.NumberSequenceIterator

object SourceApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
//    env.setParallelism(4)
//    val stream = env.fromCollection(List(
//      Access(202112120010L, "ruozedata.com", 2000),
//      Access(202112120010L, "ruoze.ke.qq.com", 6000),
//      Access(202112120010L, "github.com/ruozedata", 5000),
//      Access(202112120010L, "ruozedata.com", 4000),
//      Access(202112120010L, "ruoze.ke.qq.com", 1000)
//    ))
//
//    println("------" + stream.parallelism)
//
//    val filterStream = stream.filter(_.traffic > 4000)
//    println("------" + filterStream.parallelism)

//    val stream = env.fromElements(1, "2", 3L, true, 4D, 5F)
//    println("------" + stream.parallelism)
//
//    val mapStream = stream.map(x=>x)
//    println("------" + mapStream.parallelism)


//    val stream = env.socketTextStream("ruozedata001", 9527)
//    println("------" + stream.parallelism)
//
//    val mapStream = stream.map(x=>x)
//    println("------" + mapStream.parallelism)


//    val stream = env.readTextFile("data/access.log")
//          .map(x => {
//            val splits = x.split(",")
//            Access(splits(0).trim.toLong, splits(1).trim, splits(2).trim.toDouble)
//          })
//        println("------" + stream.parallelism)
//
//        val mapStream = stream.map(x=>x)
//        println("------" + mapStream.parallelism)


//    val stream = env.fromParallelCollection(new NumberSequenceIterator(1, 10))
//            println("------" + stream.parallelism)
//
//            val mapStream = stream.map(x=>x)
//            println("------" + mapStream.parallelism)


//    val stream = env.addSource(new AccessSource03).setParallelism(4)
//    println("------" + stream.parallelism)
//
//    val mapStream = stream.filter(_.traffic > 200).setParallelism(5)
//    println("------" + mapStream.parallelism)
//
//    stream.print().setParallelism(6)

//    env.setParallelism(4)
//    val stream = env.addSource(new RuozedataMySQLSource)
//    println("------" + stream.parallelism)
//
//    val mapStream = stream.map(x => x.toString)
//    println("------" + mapStream.parallelism)

    env.setParallelism(4)
    val stream = env.addSource(new RuozedataMySQLScalikeJDBCSource)
    println("------" + stream.parallelism)

    val mapStream = stream.map(x => x.toString)
    println("------" + mapStream.parallelism)

    mapStream.print()

    env.execute(getClass.getCanonicalName)
  }


}
