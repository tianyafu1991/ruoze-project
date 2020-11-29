package com.ruoze.bigdata.homework.day20201114

import java.util

import com.ruoze.bigdata.utils.HBaseUtils
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.hadoop.hbase.client.{HTable, Put}
import org.apache.hadoop.hbase.util.Bytes

/**
 * create 'ruozedata:province_stat','f'
 *
 *
 * 北京,20201129,12
 * 上海,20201129,13
 */
object HBaseApp {


  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val parameterTool = ParameterTool.fromArgs(args)
    val hostname = parameterTool.get("hostname", "hadoop01")
    val port = parameterTool.get("port", "9527").toInt

    val stream = env.socketTextStream(hostname, port)

    stream.map(x => {
      val splits = x.split(",")
      AccessProvinceResult(splits(0),splits(1),splits(2).toInt)
    }).addSink(new RichHBaseSink)


    env.execute(getClass.getCanonicalName)
  }

}


class RichHBaseSink extends RichSinkFunction[AccessProvinceResult] {

  var provinceHTable:HTable = _
  var puts = new util.ArrayList[Put]

  override def open(parameters: Configuration): Unit = {
    println("==========open方法==========")
    val conf: org.apache.hadoop.conf.Configuration = HBaseUtils.getHBaseConfiguration("hadoop01:2181", "ruozedata:province_stat")
    provinceHTable = HBaseUtils.getHTable(conf, "ruozedata:province_stat")
  }

  override def close(): Unit = {
    println("==========close方法==========")
    if(null != provinceHTable){
      provinceHTable.close()
    }
  }

  override def invoke(value: AccessProvinceResult, context: SinkFunction.Context[_]): Unit = {
    println("==========invoke方法==========")
    val put = new Put(Bytes.toBytes(System.currentTimeMillis()))
    put.addColumn(Bytes.toBytes("f"), Bytes.toBytes("province"), Bytes.toBytes(value.province))
    put.addColumn(Bytes.toBytes("f"), Bytes.toBytes("time"), Bytes.toBytes(value.time))
    put.addColumn(Bytes.toBytes("f"), Bytes.toBytes("cnt"), Bytes.toBytes(value.cnts))
    puts.add(put)
    provinceHTable.put(puts)
    provinceHTable.flushCommits()
  }



}


case class AccessProvinceResult(province:String,time:String,cnts:Integer)
