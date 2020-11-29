package com.ruoze.bigdata.homework.day20201114

import java.util.concurrent.{Callable, ExecutorService, TimeUnit}

import com.ruoze.bigdata.utils.ElasticsearchUtils
import org.apache.flink.api.scala._
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.concurrent.Executors
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala.async.{ResultFuture, RichAsyncFunction}
import org.elasticsearch.action.search.{SearchRequest, SearchResponse}
import org.elasticsearch.client.{RequestOptions, RestHighLevelClient}

import scala.concurrent.{ExecutionContext, Future}
import org.elasticsearch.client.asyncsearch.SubmitAsyncSearchRequest
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.{SearchHit, SearchHits}
import org.elasticsearch.search.builder.SearchSourceBuilder


object AsyncESIOApp {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val parameterTool = ParameterTool.fromArgs(args)
    val hostname = parameterTool.get("hostname", "hadoop01")
    val port = parameterTool.get("port", "9527").toInt

    val stream = env.socketTextStream(hostname, port)

    // apply the async I/O transformation
    val resultStream: DataStream[Array[String]] = AsyncDataStream.unorderedWait(stream, new AsyncESRequest, 10000, TimeUnit.MILLISECONDS, 100)

    resultStream.flatMap(x => x).print()

    env.execute(getClass.getCanonicalName)

  }


  /**
   * ruozedata.com
   * ruoze.ke.qq.com
   */
  class AsyncESRequest extends RichAsyncFunction[String, Array[String]] {

    /** The context used for the future callbacks */
    implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.directExecutor())

    var executorService: ExecutorService = _
    var restHighLevelClient: RestHighLevelClient = _

    override def open(parameters: Configuration): Unit = {
      executorService = java.util.concurrent.Executors.newFixedThreadPool(20)
      restHighLevelClient = ElasticsearchUtils.getHighLevelClient()
    }

    override def asyncInvoke(input: String, resultFuture: ResultFuture[Array[String]]): Unit = {
      val future: java.util.concurrent.Future[Array[String]] = executorService.submit(new Callable[Array[String]] {
        override def call(): Array[String] = ElasticsearchUtils.query2(restHighLevelClient, input)
      })

      val resultFutureRequested = Future {
        future.get()
      }

      resultFutureRequested.onSuccess {
        case result: Array[String] => resultFuture.complete(Iterable(result))
      }
    }


    override def close(): Unit = {
      ElasticsearchUtils.close(restHighLevelClient)
      if (null != executorService) {
        executorService.shutdown()
      }
    }


  }

}
