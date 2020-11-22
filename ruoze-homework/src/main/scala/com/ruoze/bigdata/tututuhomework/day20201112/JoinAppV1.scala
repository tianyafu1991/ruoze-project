package com.ruoze.bigdata.tututuhomework.day20201112

import com.ruoze.bigdata.tututuhomework.day20201112.Domain._
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * orderStream和orderItemStream是一对多的关系
 * 所以决定了orderStream要全存,orderItemStream要选择性存
 */
object JoinAppV1 extends Logging{

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
        //TODO 将orderStream存入redis,从redis中查出orderItemStream被缓存的数据中，是否能与该order关联上，关联上的orderItem，需要从缓存中删除

        OrderDetail().buildOrder(order).buildItem(orderItem)
      }

      case (orderId, (None, Some(orderItem))) => {
        //TODO 去redis中找orderStream是否能关联上，如果关联不上，将该orderItem存入redis
        logError("左无")
        //        OrderDetail().buildOrder(order).buildItem(orderItem)
      }
      case (orderId, (Some(order), None)) => {
        //TODO 从redis中查询出orderItemStream被缓存的数据中，是否能与该order关联上的，
        // 如果有能关联上的，做关联，并将关联上的orderItemStream数据置为过期
        // 最后将orderStream存入redis
        logError("右无")
//        OrderDetail().buildOrder(order).buildItem(orderItem)
      }
      case _ => Nil
    }.print()


    ssc.start()
    ssc.awaitTermination()
  }

}
