package com.ruoze.flink.kafka

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.api.scala._

/**
 * kafka-topics.sh --create --zookeeper hadoop:2181/kafka --partitions 3 --replication-factor 1 --topic flink_kafka_app1
 * kafka-console-producer.sh --broker-list hadoop:9092 --topic flink_kafka_app1
 */
object FlinkKafkaApp {

  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment



    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "hadoop01:9092")
    properties.setProperty("group.id", "my_group_id")

    val kafkaSource = new FlinkKafkaConsumer[String]("flink_kafka_app1", new SimpleStringSchema(), properties)
    kafkaSource.setStartFromLatest()

    val stream: DataStream[String] = env.addSource(kafkaSource)
    println(stream.parallelism)
    stream.flatMap(_.split(",")).map((_,1)).keyBy(x => x._1).sum(1).print()

    env.execute(getClass.getCanonicalName)
  }

}
