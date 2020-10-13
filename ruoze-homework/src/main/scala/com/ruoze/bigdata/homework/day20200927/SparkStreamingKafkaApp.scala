package com.ruoze.bigdata.homework.day20200927

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}


object SparkStreamingKafkaApp extends Logging {

  def getKafkaProducerParams(): Map[String, Object] = {
    val props = new Properties
    props.put("bootstrap.servers", "hadoop:9092")
    props.put("acks", "all")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    Map[String, Object](
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> props.get("bootstrap.servers"),
      ProducerConfig.ACKS_CONFIG -> props.getProperty("acks"),
//      ProducerConfig.RETRIES_CONFIG -> props.getProperty("kafka1.retries"),
//      ProducerConfig.BATCH_SIZE_CONFIG -> props.getProperty("kafka1.batch.size"),
//      ProducerConfig.LINGER_MS_CONFIG -> props.getProperty("kafka1.linger.ms"),
//      ProducerConfig.BUFFER_MEMORY_CONFIG -> props.getProperty("kafka1.buffer.memory"),
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer],
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer]
    )
  }


  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .setMaster("local")
      .setAppName(this.getClass.getSimpleName)

    val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))

    ssc.checkpoint("src\\main\\resources\\chk")
    //指定去消费streaming1这个topic
    val topics: List[String] = "streaming1" :: Nil

    val outTopic = "streaming2"

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "hadoop:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "tianya_group",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val inputDS: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    // 初始化KafkaSink,并广播
    val kafkaProducer: Broadcast[KafkaSink[String, String]] = {
      logInfo("kafka producer init done!")
      ssc.sparkContext.broadcast(KafkaSink[String, String](getKafkaProducerParams))
    }


    inputDS.foreachRDD( rdd => {
      if(!rdd.isEmpty()){
        rdd.foreachPartition(partition => {
          val kafkaSink: KafkaSink[String, String] = kafkaProducer.value

          partition.foreach(record => {
            kafkaSink.send(outTopic,record.value())
          })
          //TODO
        })
      }
    })


    ssc.start()
    ssc.awaitTermination()

  }

}
