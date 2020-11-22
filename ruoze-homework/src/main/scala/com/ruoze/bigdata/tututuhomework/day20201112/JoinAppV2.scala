package com.ruoze.bigdata.tututuhomework.day20201112

import com.ruoze.bigdata.tututuhomework.day20201112.Domain._
import com.ruoze.bigdata.tututuhomework.day20201112.utils.RedisUtils
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.execution.streaming.StreamMetadata.format
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.json4s.DefaultFormats
import org.json4s.jackson.Json

/**
 * orderStream和orderItemStream是一对多的关系
 * 所以决定了orderStream要全存,orderItemStream要选择性存
 */
object JoinAppV2 extends Logging {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val ssc = new StreamingContext(conf, Seconds(10))

    val orderHashKey = "order"
    val orderItemHashKey = "orderItem"
    val valueSeparator = "<@@@>"

    val orderStream: DStream[(Int, Order)] = ssc.socketTextStream("hadoop01", 9526).map(x => {
      val splits = x.split(",")
      (splits(0).trim.toInt, Order(splits(0).trim.toInt, splits(1).trim.toInt))
    })


    val orderItemStream: DStream[(Int, OrderItem)] = ssc.socketTextStream("hadoop01", 9527).map(x => {
      val splits = x.split(",")
      (splits(1).trim.toInt, OrderItem(splits(0).trim.toInt, splits(1).trim.toInt, splits(2).trim.toInt, splits(3).trim, splits(4).trim.toLong))
    })

    orderStream.fullOuterJoin(orderItemStream)
      .foreachRDD(rdd => {
        if(!rdd.isEmpty()){
          rdd.foreachPartition(partition => {
            //获取redis连接
            val jedis = RedisUtils.getJedis

            partition.foreach{
              case (orderId, (Some(order), Some(orderItem))) => {
                //TODO 将orderStream存入redis,从redis中查出orderItemStream被缓存的数据中，是否能与该order关联上，关联上的orderItem，需要从缓存中删除
                //orderStream 的redis存储使用hash的方式，key为order field为orderId,value为order的json格式
                //缓存并设置过期时间为1天
                jedis.hset(orderHashKey, orderId.toString, Json(DefaultFormats).writePretty(order))
                jedis.expire(orderHashKey, 60 * 60 * 24)

                //orderItemStream的redis存储使用hash的方式，key为orderItemStream，field为orderId,value为一个或多个orderItemStream的json格式的拼接
                val jsons = jedis.hget(orderItemHashKey, orderId.toString)
                var orderDetails: List[OrderDetail] = Nil
                if (null != jsons) {
                  //表示前面redis中有orderitem的缓存
                  val splits = jsons.split(valueSeparator)
                  val items: Array[OrderItem] = splits.map(json => {
                    val value = Json(DefaultFormats).parse(json)
                    val item = value.extract[OrderItem]
                    orderDetails = OrderDetail().buildOrder(order).buildItem(item) :: orderDetails
                    item
                  })
                  //把orderItemStream中关联上的数据删除
                  jedis.hdel(orderItemHashKey, orderId.toString)
                }

                orderDetails = OrderDetail().buildOrder(order).buildItem(orderItem) :: orderDetails
                logError(s"最终的结果是：${orderDetails}")
                orderDetails
              }

              case (orderId, (None, Some(orderItem))) => {
                //TODO 去redis中找order是否能关联上，如果关联不上，将该orderItem存入redis,如果关联上了，就做关联返回
                logError("左无")
                var orderDetails: List[OrderDetail] = Nil
                val orderJson = jedis.hget(orderHashKey, orderId.toString)
                if (null != orderJson) {
                  //表示能关联上
                  val order = Json(DefaultFormats).parse(orderJson).extract[Order]
                  orderDetails = OrderDetail().buildOrder(order).buildItem(orderItem) :: orderDetails
                } else {
                  //表示关联不上
                  //先查询redis中该orderId的是否存在，存在的就是json拼接，不存在的就是存入
                  val json = Json(DefaultFormats).writePretty(orderItem)
                  val orderItemJsons = jedis.hget(orderItemHashKey, orderId.toString)
                  if (null != orderItemJsons) {
                    jedis.hsetnx(orderItemHashKey, orderId.toString, orderItemJsons + valueSeparator + json)
                  } else {
                    jedis.hset(orderItemHashKey, orderId.toString, json)
                  }
                }
                logError(s"最终的结果是：${orderDetails}")
                orderDetails
              }
              case (orderId, (Some(order), None)) => {
                //TODO 从redis中查询出orderItemStream被缓存的数据中，是否能与该order关联上的，
                // 如果有能关联上的，做关联，并将关联上的orderItemStream数据置为过期
                // 最后将orderStream存入redis
                logError("右无")
                var orderDetails: List[OrderDetail] = Nil
                val jsons = jedis.hget(orderItemHashKey, orderId.toString)
                if (null != jsons) {
                  //表示前面redis中有orderitem的缓存
                  val splits = jsons.split(valueSeparator)
                  val items: Array[OrderItem] = splits.map(json => {
                    val value = Json(DefaultFormats).parse(json)
                    val item = value.extract[OrderItem]
                    orderDetails = OrderDetail().buildOrder(order).buildItem(item) :: orderDetails
                    item
                  })
                  //把orderItemStream中关联上的数据删除
                  jedis.hdel(orderItemHashKey, orderId.toString)

                }
                //将order存入redis
                jedis.hset(orderHashKey, orderId.toString, Json(DefaultFormats).writePretty(order))
                jedis.expire(orderHashKey, 60 * 60 * 24)
                logError(s"最终的结果是：${orderDetails}")
                orderDetails
              }
              case _ => Nil
            }

          })
        }else {
          logError("没有数据")
        }
      })


    ssc.start()
    ssc.awaitTermination()
  }

}
