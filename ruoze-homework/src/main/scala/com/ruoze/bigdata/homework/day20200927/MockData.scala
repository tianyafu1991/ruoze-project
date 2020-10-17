package com.ruoze.bigdata.homework.day20200927

import java.util.Properties

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

/**
 * 向kafka的第一个topic中mock access log data
 */
object MockData {


  /**
   * 创建Kafka Producer
   * 参考:http://kafka.apache.org/26/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html
   * @return
   */
  def createKafkaProducer():KafkaProducer[String,String] = {

    val props = new Properties
    props.put("bootstrap.servers", "hadoop:9092")
    props.put("acks", "all")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    new KafkaProducer[String, String](props)
    /*for (i <- 0 until 100) {
      producer.send(new ProducerRecord[String, String]("my-topic", Integer.toString(i), Integer.toString(i)))
    }null*/
  }


  def close(kafkaProducer: KafkaProducer[String, String]): Unit ={
    if (null != kafkaProducer){
      kafkaProducer.close()
    }
  }



  def main(args: Array[String]): Unit = {
    //创建producer
    val kafkaProducer: KafkaProducer[String, String] = createKafkaProducer
    val topic = "streaming1"
    for (i <- 1 to 600000){
      val accessLog: String = MockAccessLog.mockAccessLog(i)
      kafkaProducer.send(new ProducerRecord[String,String](topic,null,accessLog))
    }


    //关闭producer
    close(kafkaProducer)
  }



}
