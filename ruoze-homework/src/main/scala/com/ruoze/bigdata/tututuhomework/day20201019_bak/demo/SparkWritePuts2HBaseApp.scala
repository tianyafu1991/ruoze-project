package com.ruoze.bigdata.tututuhomework.day20201019_bak.demo

import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{HTable, Put}
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.{SparkConf, SparkContext}

object SparkWritePuts2HBaseApp {


  def main(args: Array[String]): Unit = {
    def main(args: Array[String]): Unit = {
      val sc = new SparkContext(new SparkConf().setMaster("local[2]").setAppName("hbase"))
      val rdd = sc.makeRDD(Array(1)).flatMap(_ => 0 to 1000000)
      rdd.map(value => {
        var put = new Put(Bytes.toBytes(value.toString))
        put.addColumn(Bytes.toBytes("f1"), Bytes.toBytes("c1"), Bytes.toBytes(value.toString))
        put
      }).foreachPartition(iterator => {
        var jobConf = new JobConf(HBaseConfiguration.create())
        jobConf.set("hbase.zookeeper.quorum", "172.17.11.85,172.17.11.86,172.17.11.87")
        jobConf.set("zookeeper.znode.parent", "/hbase")
        jobConf.setOutputFormat(classOf[TableOutputFormat])
        val table = new HTable(jobConf, TableName.valueOf("word"))
        import scala.collection.JavaConversions._
        table.put(seqAsJavaList(iterator.toSeq))
      })
    }

  }

}
