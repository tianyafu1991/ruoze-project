package com.ruoze.spark.streaming.day20201112

import com.ruoze.spark.streaming.day20201112.Domain.{Order, OrderDetail, OrderItem}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object JoinAppV2 {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val ssc = new StreamingContext(conf, Seconds(10))

    val orderStream: DStream[(Int, Order)] = ssc.socketTextStream("hadoop01", 9526).map(x => {
      val splits = x.split(",")
      (splits(0).trim.toInt, Order(splits(0).trim.toInt, splits(1).trim.toInt))
    })


    val orderItemStream: DStream[(Int, OrderItem)] = ssc.socketTextStream("hadoop01", 9527).map(x => {
      val splits = x.split(",")
      (splits(1).trim.toInt, OrderItem(splits(0).trim.toInt, splits(1).trim.toInt, splits(2).trim.toInt, splits(3).trim, splits(4).trim.toLong))
    })

    orderStream.fullOuterJoin(orderItemStream).map {
      case (orderId, (Some(order), Some(orderItem))) => {
        OrderDetail().buildOrder(order).buildItem(orderItem)
      }

      case (orderId, (None, Some(orderItem))) => {
        //        OrderDetail().buildOrder(order).buildItem(orderItem)
      }
      case (orderId, (Some(order), None)) => {
//        OrderDetail().buildOrder(order).buildItem(orderItem)
      }
      case _ => Nil
    }.print()


    ssc.start()
    ssc.awaitTermination()
  }

}
