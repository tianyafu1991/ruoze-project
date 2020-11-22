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
import redis.clients.jedis.Jedis

/**
 * orderStream和orderItemStream是一对多的关系
 * 所以决定了orderStream要全存,orderItemStream要选择性存
 */
object JoinAppV3 extends Logging {
  /**
   * 适合于没有order的场景，即 左无
   * orderItemStream 的redis存储使用hash的方式，key为orderItem field为orderId,value为order的json格式的拼接，以<@@@>为分隔
   *
   * @param jedis
   * @param orderId
   * @param orderItem
   * @param orderHashKey
   * @param orderItemHashKey
   * @param valueSeparator
   * @return
   */
  def handleNotHasOrder(jedis: Jedis, orderId: String, orderItem: OrderItem, orderHashKey: String, orderItemHashKey: String, valueSeparator: String): List[OrderDetail] = {
    var orderDetails: List[OrderDetail] = Nil
    //从redis中查询以前有没有缓存该orderId的order
    val orderJson = jedis.hget(orderHashKey, orderId)
    if (null != orderJson) {
      //表示能关联上
      val order = Json(DefaultFormats).parse(orderJson).extract[Order]
      orderDetails = OrderDetail().buildOrder(order).buildItem(orderItem) :: orderDetails
    } else {
      //表示关联不上
      //先查询redis中该orderId的是否存在，存在的就是json拼接，不存在的就是存入
      val json = Json(DefaultFormats).writePretty(orderItem)
      val orderItemJsons = jedis.hget(orderItemHashKey, orderId)
      if (null != orderItemJsons) {
        jedis.hsetnx(orderItemHashKey, orderId, orderItemJsons + valueSeparator + json)
      } else {
        jedis.hset(orderItemHashKey, orderId, json)
      }
    }
    orderDetails
  }

  /**
   * 适合于有order的场景，即 左右或者右无
   * orderStream 的redis存储使用hash的方式，key为order field为orderId,value为order的json格式,设置过期时间为 1 天
   *
   * @param jedis            redis连接客户端
   * @param orderId          本次的orderId
   * @param order            本次的order
   * @param orderItem        本次的orderItem，可能为null
   * @param orderHashKey     order缓存的Hash Key
   * @param orderItemHashKey orderItem缓存的Hash Key
   * @param valueSeparator   多个orderItem之间在redis以这个作为分隔符
   * @return
   */
  def handleHasOrder(jedis: Jedis, orderId: String, order: Order, orderItem: OrderItem, orderHashKey: String, orderItemHashKey: String, valueSeparator: String): List[OrderDetail] = {
    var orderDetails: List[OrderDetail] = Nil
    //从redis中查询出是否有对应orderId的orderItem缓存着
    val jsons = jedis.hget(orderItemHashKey, orderId)
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
      jedis.hdel(orderItemHashKey, orderId)
    }
    //本次orderItem是否不为null
    if (null != orderItem) {
      orderDetails = OrderDetail().buildOrder(order).buildItem(orderItem) :: orderDetails
    }

    //将order存入redis
    jedis.hset(orderHashKey, orderId, Json(DefaultFormats).writePretty(order))
    jedis.expire(orderHashKey, 60 * 60 * 24)
    orderDetails
  }

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val ssc = new StreamingContext(conf, Seconds(10))

    val orderHashKey: String = "order"
    val orderItemHashKey: String = "orderItem"
    val valueSeparator: String = "<@@@>"

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
        if (!rdd.isEmpty()) {
          rdd.foreachPartition(partition => {
            //获取redis连接
            val jedis: Jedis = RedisUtils.getJedis

            partition.foreach {
              case (orderId, (Some(order), Some(orderItem))) => {
                val orderDetails: List[OrderDetail] = handleHasOrder(jedis, orderId.toString, order, orderItem, orderHashKey, orderItemHashKey, valueSeparator)
                logError(s"最终的结果是：${orderDetails}")
                orderDetails
              }
              case (orderId, (None, Some(orderItem))) => {
                logError("左无")
                val orderDetails: List[OrderDetail] = handleNotHasOrder(jedis, orderId.toString, orderItem, orderHashKey, orderItemHashKey, valueSeparator)
                logError(s"最终的结果是：${orderDetails}")
                orderDetails
              }
              case (orderId, (Some(order), None)) => {
                logError("右无")
                val orderDetails: List[OrderDetail] = handleHasOrder(jedis, orderId.toString, order, null, orderHashKey, orderItemHashKey, valueSeparator)
                logError(s"最终的结果是：${orderDetails}")
                orderDetails
              }
              case _ => Nil
            }

          })
        } else {
          logError("没有数据")
        }
      })


    ssc.start()
    ssc.awaitTermination()
  }


}
