package com.ruoze.flink.sink

import java.util

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.Requests

object SinkApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val result: DataStream[(String, Double)] = env.readTextFile("data/access.txt")
      .map(x => {
        val splits = x.split(",")
        (splits(1).trim, splits(2).trim.toDouble)
      }).keyBy(x => x._1).sum(1)


    val sinkFunction: ElasticsearchSinkFunction[(String, Double)] = new ElasticsearchSinkFunction[(String, Double)] {
      override def process(t: (String, Double), runtimeContext: RuntimeContext, requestIndexer: RequestIndexer) = {
        val json = new java.util.HashMap[String, String]
        json.put("domain", t._1)
        json.put("traffics", t._2.toString)
        val request: IndexRequest = Requests.indexRequest()
          .index("ruozedata_flink_access")
          .source(json)
          .id(t._1)

        requestIndexer.add(request)

      }
    }

    // 定义HttpHosts
    val httpHosts = new util.ArrayList[HttpHost]()
    httpHosts.add(new HttpHost("hadoop01", 9200))

//    result.addSink(new ElasticsearchSink.Builder[(String,Double)](httpHosts,sinkFunction))
    env.execute(getClass.getCanonicalName)
  }

}
